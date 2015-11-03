package rdb2rdf.graph

import rdb2rdf.graph.DatabaseGraph.Edge.Edge
import rdb2rdf.graph.DatabaseGraph.{ColumnVertex, DatabaseVertex, TableVertex, Vertex}

import scala.collection.mutable

class DatabaseGraph(
  vertices: Set[Vertex],
  edges: Set[(Vertex, Edge, Vertex)])
  extends DirectGraph(vertices, edges) {

  def getAllDatabases = vertices.collect {
    case v: DatabaseVertex => v
  }

  def getAllTables = vertices.collect {
    case v: TableVertex => v
  }

  def getAllColumns = vertices.collect {
    case v: ColumnVertex => v
  }

  def getPrimaryKey(v: DatabaseGraph.TableVertex) =
    connectedVerticesWithEdgeFilter(v)(_ == DatabaseGraph.Edge.PrimaryKey).collectFirst {
      case v: ColumnVertex => v
    }

  def getForeignKeys(v: DatabaseGraph.TableVertex) =
    connectedVerticesWithEdgeFilter(v)(_ == DatabaseGraph.Edge.ForeignKey).collect {
      case v: ColumnVertex => v
    }

  def getDatabase(v: DatabaseGraph.TableVertex) =
    connectedVerticesWithEdgeFilter(v)(_ == DatabaseGraph.Edge.Belongs).collectFirst {
      case v: DatabaseVertex => v
    }

  def getTables(v: DatabaseGraph.DatabaseVertex) =
    connectedVerticesWithEdgeFilter(v)(_ == DatabaseGraph.Edge.Contains).collect {
      case v: TableVertex => v
    }

  def getColumns(v: DatabaseGraph.TableVertex) =
    connectedVerticesWithEdgeFilter(v)(_ == DatabaseGraph.Edge.Contains).collect {
      case v: ColumnVertex => v
    }

  def ++(otherGraph: DatabaseGraph): DatabaseGraph = {
    val g1 = if (isGeneralized) this else generalized
    val g2 = if (otherGraph.isGeneralized) otherGraph else otherGraph.generalized

    new DatabaseGraph(g1.vertices ++ g2.vertices, g1.edges ++ g2.edges)
  }

  def isGeneralized = (getAllTables ++ getAllColumns).forall {
    case v: DatabaseVertex => true
    case v: Vertex => v.identifier.contains("$")
  }

  private[graph] def generalized: DatabaseGraph = {
    if (isGeneralized) {
      throw new IllegalStateException("The graph is already generalized!")
    }

    val verticesMap = mutable.Map[String, Vertex]()
    val newEdges = mutable.ArrayBuffer[(Vertex, Edge, Vertex)]()
    val dbVertex = getAllDatabases.toSeq.head

    verticesMap += ((dbVertex.identifier, dbVertex))
    verticesMap ++= getAllTables.map(v => (v.identifier, v.copy(prefix = Some(dbVertex.identifier))))
    verticesMap ++= getAllColumns.map(v => (v.identifier, v.copy(table = verticesMap(v.table).identifier)))

    newEdges ++= edges.map(e => (verticesMap(e._1.identifier), e._2, verticesMap(e._3.identifier)))

    new DatabaseGraph(verticesMap.values.toSet, newEdges.toSet)
  }
}

object DatabaseGraph {

  abstract class Vertex {
    def identifier: String
  }

  case class DatabaseVertex(
    jdbcUrl: String,
    name: Option[String] = None) extends Vertex {

    override def identifier = name match {
      case Some(value) => value
      case None => s"$jdbcUrl:database"
    }

    override def toString = s"DatabaseVertex($identifier)"
  }

  case class TableVertex(
    name: String,
    prefix: Option[String] = None) extends Vertex {

    override def identifier = prefix match {
      case Some(value) => s"$value$$$name"
      case None => name
    }

    override def toString = s"TableVertex($identifier)"
  }

  case class ColumnVertex(
    name: String,
    table: String,
    dataType: Int) extends Vertex {

    override def identifier = s"$table/$name"

    override def toString = s"ColumnVertex($identifier)"
  }

  object Edge extends Enumeration {
    type Edge = Value

    val Contains, Belongs, ForeignKey, PrimaryKey = Value
  }

  class Builder {
    private var databaseJdbcUrl: Option[String] = None
    private var databaseName: Option[String] = None
    private var tables: Option[Seq[String]] = None
    private var columns: Option[Seq[(String, String, Int)]] = None
    private var primaryKeys: Option[Seq[(String, String)]] = None
    private var foreignKeys: Option[Map[(String, String), (String, String)]] = None

    def setDatabaseJdbcUrl(data: String) = {
      databaseJdbcUrl = Some(data)
      this
    }

    def setDatabaseName(data: String) = {
      databaseName = Some(data)
      this
    }

    def setTables(data: Seq[String]) = {
      tables = Some(data)
      this
    }

    def setColumns(data: Seq[(String, String, Int)]) = {
      columns = Some(data)
      this
    }

    def setPrimaryKeys(data: Seq[(String, String)]) = {
      primaryKeys = Some(data)
      this
    }

    def setForeignKeys(data: Map[(String, String), (String, String)]) = {
      foreignKeys = Some(data)
      this
    }

    private[graph] def validate(): Unit = {
      databaseJdbcUrl match {
        case Some(_) =>
        case None => throw new IllegalArgumentException("JDBC URL of database is required!")
      }

      tables match {
        case Some(tbls) =>
          if (tbls.size != tbls.toSet.size) {
            throw new IllegalArgumentException("There are some tables with name conflict!")
          }
        case None =>
      }

      columns match {
        case Some(cols) =>
          if (tables.isEmpty) {
            throw new IllegalArgumentException("Columns exist, but there is no table. ")
          }

          val names = cols.map(c => (c._1, c._2))
          if (names.size != names.toSet.size) {
            throw new IllegalArgumentException("There are some columns with name conflict!")
          }

          if (!cols.forall(c => tables.get.contains(c._1))) {
            throw new IllegalArgumentException("Some columns refer to wrong table.")
          }
        case None =>
      }

      primaryKeys match {
        case Some(pks) =>
          if (tables.isEmpty || columns.isEmpty) {
            throw new IllegalArgumentException("There are some primary keys, but there is no tables or no columns.")
          }

          val cols = columns.get.map(c => (c._1, c._2))
          if (!pks.forall(k => tables.get.contains(k._1) && cols.contains((k._1, k._2)))) {
            throw new IllegalArgumentException("Some primary key refer to wrong column.")
          }
        case None =>
      }

      foreignKeys match {
        case Some(fks) =>
          if (tables.isEmpty || columns.isEmpty || primaryKeys.isEmpty) {
            throw new IllegalArgumentException(
              "There are some foreign keys, but there is no tables, no columns or no primary keys.")
          }

          val cols = columns.get.map(c => (c._1, c._2))
          if (!fks.forall {
            k => tables.get.contains(k._1._1) && tables.get.contains(k._2._1) &&
              cols.contains(k._1) && cols.contains(k._2)
          }) {
            throw new IllegalArgumentException("Some foreign key refer to wrong column.")
          }
        case None =>
      }
    }

    def build(): DatabaseGraph = {
      validate()

      val dbVertex = DatabaseVertex(databaseJdbcUrl.get, databaseName)
      val tableVertices = tables match {
        case Some(vs) => vs.map(table => TableVertex(table))
        case None => Seq[TableVertex]()
      }
      val columnVertices = columns match {
        case Some(vs) => vs.map { case (table, column, dataType) =>
          ColumnVertex(column, table, dataType)
        }
        case None => Seq[ColumnVertex]()
      }
      val vertices = tableVertices ++ columnVertices ++ Seq(dbVertex)
      val verticesMap = vertices.map(v => (v.identifier, v)).toMap

      val dbTableEdges = tableVertices.map((dbVertex, Edge.Contains, _)) ++
        tableVertices.map((_, Edge.Belongs, dbVertex))

      val tableColumnEdges = columnVertices.map { v => (verticesMap(v.table), Edge.Contains, v) } ++
        columnVertices.map { v => (v, Edge.Belongs, verticesMap(v.table)) }

      val primaryKeyEdges = primaryKeys match {
        case Some(pk) => columnVertices
          .filter { v => pk.contains((v.table, v.name)) }
          .map { v => (verticesMap(v.table), Edge.PrimaryKey, v) }
        case None => Seq[(Vertex, Edge.Edge, Vertex)]()
      }

      val foreignKeyEdges = foreignKeys match {
        case Some(fk) => columnVertices
          .filter { v => fk.contains((v.table, v.name)) }
          .map { v =>
            val link = fk((v.table, v.name))
            (v, Edge.ForeignKey, verticesMap(s"${link._1}/${link._2}"))
          }
        case None => Seq[(Vertex, Edge.Edge, Vertex)]()
      }

      val edges = dbTableEdges ++ tableColumnEdges ++ primaryKeyEdges ++ foreignKeyEdges

      new DatabaseGraph(vertices.toSet, edges.toSet)
    }
  }

}