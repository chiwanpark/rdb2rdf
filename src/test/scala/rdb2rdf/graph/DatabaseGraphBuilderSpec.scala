package rdb2rdf.graph

import org.scalatest.{FlatSpec, Matchers}
import org.slf4j.LoggerFactory
import rdb2rdf.graph.DatabaseGraph.{DatabaseVertex, Builder}

class DatabaseGraphBuilderSpec extends FlatSpec with Matchers {
  behavior of "DatabaseGraph.Builder"

  val LOG = LoggerFactory.getLogger(this.getClass)

  it can "check database URL before creating graph" in {
    a[IllegalArgumentException] should be thrownBy {
      new Builder().build()
    }
  }

  it should "create simple graph" in {
    val jdbcUrl = "jdbc:test"
    val graph = new Builder().setDatabaseJdbcUrl(jdbcUrl).build()

    graph.vertices.size should equal(1)
    graph.vertices.head should be(a[DatabaseVertex])
    graph.vertices.head.asInstanceOf[DatabaseVertex].jdbcUrl should equal(jdbcUrl)
  }

  it should "create complex graph" in {
    val jdbcUrl = "jdbc:test"
    val dbName = "testdb"
    val tables = Seq("table1", "table2")
    val columns = Seq(("table1", "id", 4), ("table1", "name", 12), ("table2", "id", 4), ("table2", "table1_id", 4))
    val primaryKeys = Seq(("table1", "id"), ("table2", "id"))
    val foreignKeys = Map(("table2", "table1_id") -> ("table1", "id"))

    val graph = new Builder()
      .setDatabaseJdbcUrl(jdbcUrl)
      .setDatabaseName(dbName)
      .setTables(tables)
      .setColumns(columns)
      .setPrimaryKeys(primaryKeys)
      .setForeignKeys(foreignKeys)
      .build()

    graph.vertices.size should equal(7)
    graph.edges.size should equal(15)
  }
}
