package rdb2rdf.database

import java.io.File
import java.nio.file.Files
import java.sql.{DriverManager, SQLException}

import org.scalatest.{FlatSpec, Matchers}
import org.slf4j.LoggerFactory
import rdb2rdf.models.{ColumnType, Database, DatabaseColumn}

class DatabaseInspectorSpec extends FlatSpec with Matchers {
  behavior of "DatabaseInspector"

  val LOG = LoggerFactory.getLogger(this.getClass)
  val tempDir = Files.createTempDirectory("inspector_spec_")

  try {
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        (getFilesInDirectory(tempDir.toFile) :+ tempDir.toFile).foreach { f =>
          try {
            f.delete()
          } catch {
            case t: Throwable => LOG.warn("Error while deleting file: " + f.getAbsolutePath, t)
          }
        }
      }
    })
  } catch {
    case t: Throwable => LOG.warn("Error while adding shutdown hook.", t)
  }

  it should "can inspect H2 Database" in {
    val url = "jdbc:h2:" + tempDir.toString + File.separator + "test_h2"
    createSampleTable(url)

    checkDatabaseUpper(url, DatabaseInspector.inspect(url))
  }

  it should "can inspect PostgreSQL database" in {
    val url = "jdbc:postgresql://localhost:5432/travis_ci_test"
    try {
      createSampleTable(url, Some("postgres"))
      checkDatabaseLower(url, DatabaseInspector.inspect(url, Some("postgres")))
    } catch {
      case e: SQLException if e.toString contains "refused" =>
        LOG.warn("Test for PostgreSQL is not executed. Please check PostgreSQL server.", e)
    }
  }

  it should "can inspect SQLite database" in {
    val url = "jdbc:sqlite:" + tempDir.toString + File.separator + "test_sqlite.db"
    createSampleTable(url)

    checkDatabaseUpper(url, DatabaseInspector.inspect(url), primaryKey = false)
  }

  it should "can inspect MySQL database" in {
    val url = "jdbc:mysql://localhost:3306/travis_ci_test"
    try {
      createSampleTable(url, Some("root"))
      checkDatabaseUpper(url, DatabaseInspector.inspect(url, Some("root")))
    } catch {
      case e: SQLException if e.toString contains "refused" =>
        LOG.warn("Test for MySQL is not executed. Please check MySQL server.", e)
    }
  }

  private def checkDatabaseUpper(url: String, database: Database, primaryKey: Boolean = true): Unit = {
    database.url should equal(url)
    database.tables.length should equal(2)

    val (tbl1, tbl2) = (database.tables.head, database.tables(1))
    tbl1.tableName should equal("SAMPLE1")
    tbl1.columns.length should equal(2)

    val (t1Col1, t1Col2) = (tbl1.columns.head, tbl1.columns(1))
    t1Col1 should equal(DatabaseColumn("ID", ColumnType.Integer, primaryKey = primaryKey))
    t1Col2 should equal(DatabaseColumn("NAME", ColumnType.String))

    tbl2.tableName should equal("SAMPLE2")
    tbl2.columns.length should equal(3)

    val (t2Col1, t2Col2, t2Col3) = (tbl2.columns.head, tbl2.columns(1), tbl2.columns(2))
    t2Col1 should equal(DatabaseColumn("ID", ColumnType.Integer, primaryKey = primaryKey))
    t2Col2 should equal(DatabaseColumn("NAME", ColumnType.String))
    t2Col3 should equal(DatabaseColumn("SAMPLE1_ID", ColumnType.Integer, Some("SAMPLE1", "ID")))
  }

  private def checkDatabaseLower(url: String, database: Database): Unit = {
    database.url should equal(url)
    database.tables.length should equal(2)

    val (tbl1, tbl2) = (database.tables.head, database.tables(1))
    tbl1.tableName should equal("sample1")
    tbl1.columns.length should equal(2)

    val (t1Col1, t1Col2) = (tbl1.columns.head, tbl1.columns(1))
    t1Col1 should equal(DatabaseColumn("id", ColumnType.Integer, primaryKey = true))
    t1Col2 should equal(DatabaseColumn("name", ColumnType.String))

    tbl2.tableName should equal("sample2")
    tbl2.columns.length should equal(3)

    val (t2Col1, t2Col2, t2Col3) = (tbl2.columns.head, tbl2.columns(1), tbl2.columns(2))
    t2Col1 should equal(DatabaseColumn("id", ColumnType.Integer, primaryKey = true))
    t2Col2 should equal(DatabaseColumn("name", ColumnType.String))
    t2Col3 should equal(DatabaseColumn("sample1_id", ColumnType.Integer, Some("sample1", "id")))
  }

  private def createSampleTable(
    url: String, username: Option[String] = None, password: Option[String] = None): Unit = {
    val connection = DriverManager.getConnection(url, username.orNull, password.orNull)

    try {
      val statement = connection.createStatement()
      statement.executeUpdate("DROP TABLE IF EXISTS SAMPLE2")
      statement.executeUpdate("DROP TABLE IF EXISTS SAMPLE1")

      statement.executeUpdate("CREATE TABLE SAMPLE1 (ID INT PRIMARY KEY, NAME VARCHAR(30));")
      statement.executeUpdate("INSERT INTO SAMPLE1 (ID, NAME) VALUES (1, 'sample');")

      statement.executeUpdate(
        "CREATE TABLE SAMPLE2 (ID INT PRIMARY KEY, NAME VARCHAR(30), SAMPLE1_ID INT, " +
          "FOREIGN KEY (SAMPLE1_ID) REFERENCES SAMPLE1(ID));")
      statement.executeUpdate("INSERT INTO SAMPLE2 (ID, NAME, SAMPLE1_ID) VALUES (1, 'sample', 1);")
      statement.close()
    } finally {
      connection.close()
    }
  }

  private def getFilesInDirectory(path: File): Seq[File] = {
    path.listFiles.filter(_.isDirectory).flatMap(getFilesInDirectory) ++ path.listFiles
  }
}
