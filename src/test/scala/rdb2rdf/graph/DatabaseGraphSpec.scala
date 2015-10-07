package rdb2rdf.graph

import org.scalatest.{FlatSpec, Matchers}
import rdb2rdf.graph.DatabaseGraph.{Builder, DatabaseVertex, Vertex}

class DatabaseGraphSpec extends FlatSpec with Matchers {
  behavior of "DatabaseGraph"

  it can "be merged with other graph" in {
    val graph1 = createSampleGraph()
    val graph2 = createSampleGraph("testdb2")
    val merged = graph1 ++ graph2

    merged.vertices.size should equal(graph1.vertices.size + graph2.vertices.size)
    merged.edges.size should equal(graph1.edges.size + graph2.edges.size)
    merged.isGeneralized should equal(true)
  }

  it can "prevent generalizing the already generalized graph" in {
    val graph = createSampleGraph()

    a[IllegalStateException] should be thrownBy {
      graph.generalized.generalized
    }
  }

  it can "generalize the given graph" in {
    val graph = createSampleGraph()
    val generalized = graph.generalized

    generalized.vertices.size should equal(graph.vertices.size)
    generalized.edges.size should equal(graph.edges.size)
    generalized.getAllDatabases should equal(graph.getAllDatabases)
    (generalized.getAllTables ++ generalized.getAllColumns).forall {
      case v: DatabaseVertex => true
      case v: Vertex => v.identifier.contains("$")
    } should equal(true)
  }

  private def createSampleGraph(dbName: String = "testdb1"): DatabaseGraph = {
    val jdbcUrl = "jdbc:test"
    val tables = Seq("table1", "table2")
    val columns = Seq(("table1", "id", 4), ("table1", "name", 12), ("table2", "id", 4), ("table2", "table1_id", 4))
    val primaryKeys = Seq(("table1", "id"), ("table2", "id"))
    val foreignKeys = Map(("table2", "table1_id") -> ("table1", "id"))

    new Builder()
      .setDatabaseJdbcUrl(jdbcUrl)
      .setDatabaseName(dbName)
      .setTables(tables)
      .setColumns(columns)
      .setPrimaryKeys(primaryKeys)
      .setForeignKeys(foreignKeys)
      .build()
  }
}
