package rdb2rdf.database

import java.sql.{Connection, DriverManager}

import rdb2rdf.models.{DatabaseColumn, ColumnType, Database, DatabaseTable}

import scala.collection.mutable

/** Database inspector which retrieve information from given database URL */
object DatabaseInspector {
  private def getJdbcConnection(url: String): Connection = DriverManager.getConnection(url)

  private def getTables(connection: Connection): Seq[DatabaseTable] = {
    val metadata = connection.getMetaData
    val tables = mutable.ArrayBuffer[DatabaseTable]()
    val tblResult = metadata.getTables(null, null, null, null)

    while (tblResult.next()) {
      val tblType = tblResult.getString("TABLE_TYPE")
      if (tblType == "TABLE") {
        val schema = tblResult.getString("TABLE_SCHEM")
        val catalog = tblResult.getString("TABLE_CAT")
        val tblName = tblResult.getString("TABLE_NAME")
        val columns = mutable.ArrayBuffer[DatabaseColumn]()
        val colResult = metadata.getColumns(catalog, schema, tblName, null)

        while (colResult.next()) {
          val colName = colResult.getString("COLUMN_NAME")
          val dataType = colResult.getInt("DATA_TYPE")

          columns += DatabaseColumn(colName.toLowerCase, ColumnType.fromSqlType(dataType))
        }

        tables += DatabaseTable(tblName.toLowerCase, columns.toSeq)
      }
    }

    tables.toSeq
  }

  /** Retrieve database information from given database URL.
    *
    * @param url JDBC URL for database
    * @return [[rdb2rdf.models.Database]] object containing database information
    */
  def inspect(url: String): Database = {
    val connection = getJdbcConnection(url)
    try {
      val tables = getTables(connection)
      Database(url, tables)
    } finally {
      connection.close()
    }
  }
}
