package rdb2rdf.database

import java.sql.{Connection, DriverManager}

import org.slf4j.LoggerFactory

import scala.collection.mutable

/** Database inspector which retrieve information from given database URL
  *
  * @param jdbcUrl URL for database connection
  * @param username Username for database connection if needed
  * @param password Password for database connection if needed
  */
class DatabaseInspector(jdbcUrl: String, username: Option[String] = None, password: Option[String] = None) {
  val LOG = LoggerFactory.getLogger(this.getClass)

  /** Get JDBC connection */
  def getJdbcConnection: Connection =
    DriverManager.getConnection(jdbcUrl, username.orNull, password.orNull)

  /** Returns all table names in the connected database.
    *
    * @param connection The JDBC connection to fetch table names
    * @return The list of table names
    */
  def getTables(connection: Connection): Seq[String] = {
    val metadata = connection.getMetaData
    val tables = mutable.ArrayBuffer[String]()
    val tblResult = metadata.getTables(null, "%", "%", null)

    while (tblResult.next()) {
      val tblType = tblResult.getString("TABLE_TYPE")
      if (tblType == "TABLE" || tblType == "VIEW") {
        val tblName = tblResult.getString("TABLE_NAME")

        tables += tblName
      }
    }

    tables.toSeq
  }

  /** Returns all primary keys in the connected database.
    *
    * Each primary key is represented as (table name, column name).
    *
    * @param connection The JDBC connection to fetch all primary keys
    * @return The list of primary keys
    */
  def getPrimaryKeys(connection: Connection): Seq[(String, String)] = {
    val tables = getTables(connection)
    val primaryKeys = mutable.ArrayBuffer[(String, String)]()

    tables.foreach { table =>
      val pkQryResult = connection.getMetaData.getPrimaryKeys(null, null, table)
      while (pkQryResult.next()) {
        val tblName = pkQryResult.getString("TABLE_NAME")
        val colName = pkQryResult.getString("COLUMN_NAME")

        primaryKeys += ((tblName, colName))
      }
    }

    primaryKeys.toSeq
  }

  /** Returns all foreign keys in the connected database.
    *
    * Each column is represented as (table name, column name).
    *
    * @param connection The JDBC connection to fetch all foreign keys
    * @return The map between foreign keys (key) and primary keys (value)
    */
  def getForeignKeys(connection: Connection): Map[(String, String), (String, String)] = {
    val tables = getTables(connection)
    val foreignKeys = mutable.Map[(String, String), (String, String)]()

    tables.foreach { table =>
      val fkQryResult = connection.getMetaData.getImportedKeys(null, null, table)
      while (fkQryResult.next()) {
        val fkTblName = fkQryResult.getString("FKTABLE_NAME")
        val fkColName = fkQryResult.getString("FKCOLUMN_NAME")
        val pkTblName = fkQryResult.getString("PKTABLE_NAME")
        val pkColName = fkQryResult.getString("PKCOLUMN_NAME")

        foreignKeys((fkTblName, fkColName)) = (pkTblName, pkColName)
      }
    }

    foreignKeys.toMap
  }

  /** Returns all columns in the connected database.
    *
    * Each column is represented as (table name, column name, type of data).
    *
    * @param connection The JDBC connection to fetch all columns.
    * @return The list of columns
    */
  def getColumns(connection: Connection): Seq[(String, String, Int)] = {
    val columns = mutable.ArrayBuffer[(String, String, Int)]()

    getTables(connection).foreach { table =>
      val colQryResult = connection.getMetaData.getColumns(null, "%", table, "%")
      while (colQryResult.next()) {
        val tblName = colQryResult.getString("TABLE_NAME")
        val colName = colQryResult.getString("COLUMN_NAME")
        val dataType = colQryResult.getInt("DATA_TYPE")

        columns += ((tblName, colName, dataType))
      }
    }

    columns.toSeq
  }
}
