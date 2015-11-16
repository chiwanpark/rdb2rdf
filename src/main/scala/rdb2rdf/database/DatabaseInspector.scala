package rdb2rdf.database

import java.sql.{Connection, DriverManager}

import org.slf4j.LoggerFactory
import rdb2rdf.database.representation.{Constraint, Table, Column, Database}

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
  private[database] def createJdbcConnection(): Connection =
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

  def inspect(): Database = {
    val connection = createJdbcConnection()

    try {
      val primaryKeys = getPrimaryKeys(connection)
      val foreignkeys = getForeignKeys(connection)

      val columns = getColumns(connection)
      val tables = getTables(connection).map { x =>
        val memberColumns = columns.filter(_._1 == x).map(c => Column(c._2, c._3)).toSet
        val memberPks = primaryKeys.filter(_._1 == x).map { c =>
          val pkColumn = memberColumns.find(_.name == c._2) match {
            case Some(column) => column
            case None => throw new IllegalStateException("Wrong primary key description in the given database.")
          }

          Constraint(pkColumn, Constraint.Description.PrimaryKey, None)
        }.toSet
        val memberFks = foreignkeys.filter { case (src, dst) => src._1 == x }.map { case (src, dst) =>
            val srcColumn = memberColumns.find(_.name == src._2) match {
              case Some(column) => column
              case None => throw new IllegalStateException("Wrong foreign key description in the given database.")
            }

            Constraint(srcColumn, Constraint.Description.ForeignKey, Some(s"${dst._1}/${dst._2}"))
        }.toSet

        Table(x, memberColumns, memberPks ++ memberFks)
      }.toSet

      Database(jdbcUrl, username, password, tables)
    } finally {
      connection.close()
    }
  }
}
