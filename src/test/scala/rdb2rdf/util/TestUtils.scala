package rdb2rdf.util

import rdb2rdf.graph.DatabaseGraph
import rdb2rdf.graph.DatabaseGraph.Builder

object TestUtils {

  /** Creates a database graph which is simple and contains two tables with one-to-many relationships.
    *
    * @param dbName The name of database
    * @return [[DatabaseGraph]] object which represents database graph mentioned above.
    */
  def createSimpleGraph(dbName: String = "testdb1"): DatabaseGraph = {
    val jdbcUrl = "jdbc:test"
    val tables = Seq("table1", "table2")
    val columns = Seq(("table1", "id", 4), ("table1", "name", 12), ("table2", "id", 4), ("table2", "table1_id", 4))
    val primaryKeys = Seq(("table1", "id"), ("table2", "id"))
    val foreignKeys = Map(("table2", "table1_id") ->("table1", "id"))

    new Builder()
      .setDatabaseJdbcUrl(jdbcUrl)
      .setDatabaseName(dbName)
      .setTables(tables)
      .setColumns(columns)
      .setPrimaryKeys(primaryKeys)
      .setForeignKeys(foreignKeys)
      .build()
  }

  /** Creates a database graph which contains three tables with many-to-many relationships.
    *
    * @param dbName The name of database
    * @return [[DatabaseGraph]] object which represents database graph mentioned above
    */
  def createComplexGraph(dbName: String = "testdb1"): DatabaseGraph = {
    val jdbcUrl = "jdbc:test"
    val tables = Seq("table1", "table2", "table1_table2_link")
    val columns = Seq(
      ("table1", "id", 4),
      ("table1", "name", 12),
      ("table2", "id", 4),
      ("table2", "name", 12),
      ("table1_table2_link", "id", 4),
      ("table1_table2_link", "table1_id", 4),
      ("table1_table2_link", "table2_id", 4)
    )
    val primaryKeys = Seq(("table1", "id"), ("table2", "id"), ("table1_table2_link", "id"))
    val foreignKeys = Map(
      ("table1_table2_link", "table1_id") ->("table1", "id"),
      ("table1_table2_link", "table2_id") ->("table2", "id")
    )

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
