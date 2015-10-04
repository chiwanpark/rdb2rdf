package rdb2rdf.database

import java.sql.{Connection, DatabaseMetaData, DriverManager}

import org.slf4j.LoggerFactory
import rdb2rdf.models.{ColumnType, Database, DatabaseColumn, DatabaseTable}

import scala.collection.mutable

/** Database inspector which retrieve information from given database URL */
object DatabaseInspector {
  val LOG = LoggerFactory.getLogger(this.getClass)

  private def getJdbcConnection(
    url: String,
    username: Option[String],
    password: Option[String]): Connection = {

    LOG.info(s"Get JDBC Connection: $url")
    DriverManager.getConnection(url, username.orNull, password.orNull)
  }

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
        val columns = getColumns(metadata, catalog, schema, tblName)

        tables += DatabaseTable(tblName, columns)
      }
    }

    tables.toSeq
  }

  private def getPrimaryKeys(
    metadata: DatabaseMetaData,
    catalogName: String,
    schemaName: String,
    tableName: String): Map[String, Boolean] = {

    val primaryKeys = mutable.Map[String, Boolean]()
    val pkQryResult = metadata.getPrimaryKeys(catalogName, schemaName, tableName)
    while (pkQryResult.next()) {
      val colName = pkQryResult.getString("COLUMN_NAME")
      primaryKeys(colName) = true
    }

    primaryKeys.toMap
  }

  private def getForeignKeys(
    metadata: DatabaseMetaData,
    catalogName: String,
    schemaName: String,
    tableName: String): Map[String, (String, String)] = {

    val foreignKeys = mutable.Map[String, (String, String)]()
    val fkQryResult = metadata.getImportedKeys(catalogName, schemaName, tableName)

    while (fkQryResult.next()) {
      val fkName = fkQryResult.getString("FKCOLUMN_NAME")
      val pkTblName = fkQryResult.getString("PKTABLE_NAME")
      val pkColName = fkQryResult.getString("PKCOLUMN_NAME")

      foreignKeys(fkName) = (pkTblName, pkColName)
    }

    foreignKeys.toMap
  }

  private def getColumns(
    metadata: DatabaseMetaData,
    catalogName: String,
    schemaName: String,
    tableName: String): Seq[DatabaseColumn] = {

    val primaryKey = getPrimaryKeys(metadata, catalogName, schemaName, tableName)
    val foreignKey = getForeignKeys(metadata, catalogName, schemaName, tableName)

    val columns = mutable.ArrayBuffer[DatabaseColumn]()
    val colQryResult = metadata.getColumns(catalogName, schemaName, tableName, null)

    while (colQryResult.next()) {
      val colName = colQryResult.getString("COLUMN_NAME")
      val dataType = colQryResult.getInt("DATA_TYPE")

      val column = DatabaseColumn(colName, ColumnType.fromSqlType(dataType),
        foreignKey = foreignKey.get(colName),
        primaryKey = primaryKey.getOrElse(colName, false))

      columns += column
    }

    columns.toSeq
  }

  /** Retrieve database information from given database URL.
    *
    * @param url JDBC URL for database
    * @return [[rdb2rdf.models.Database]] object containing database information
    */
  def inspect(
    url: String,
    username: Option[String] = None,
    password: Option[String] = None): Database = {

    val connection = getJdbcConnection(url, username, password)
    try {
      val tables = getTables(connection)
      Database(url, tables)
    } finally {
      connection.close()
    }
  }
}
