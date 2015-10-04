package rdb2rdf.database

import java.io.File
import java.nio.file.Files
import java.sql.{DriverManager, SQLException}

import org.scalatest.{FlatSpec, Matchers}
import org.slf4j.LoggerFactory
import rdb2rdf.models.{ColumnType, Database}

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

    checkDatabase(url, DatabaseInspector.inspect(url))
  }

  it should "can inspect PostgreSQL database" in {
    val url = "jdbc:postgresql://localhost:5432/travis_ci_test"
    try {
      createSampleTable(url, Some("postgres"))
      checkDatabase(url, DatabaseInspector.inspect(url, Some("postgres")))
    } catch {
      case e: SQLException if e.toString contains "refused" =>
        LOG.warn("Test for PostgreSQL is not executed. Please check PostgreSQL server.", e)
    }
  }

  it should "can inspect SQLite database" in {
    val url = "jdbc:sqlite:" + tempDir.toString + File.separator + "test_sqlite.db"
    createSampleTable(url)

    checkDatabase(url, DatabaseInspector.inspect(url))
  }

  it should "can inspect MySQL database" in {
    val url = "jdbc:mysql://localhost:3306/travis_ci_test"
    try {
      createSampleTable(url, Some("root"))
      checkDatabase(url, DatabaseInspector.inspect(url, Some("root")))
    } catch {
      case e: SQLException if e.toString contains "refused" =>
        LOG.warn("Test for MySQL is not executed. Please check MySQL server.", e)
    }
  }

  private def checkDatabase(url: String, database: Database): Unit = {
    database.url should equal(url)
    database.tables.length should equal(1)

    val tbl = database.tables.head
    tbl.tableName should equal("sample")
    tbl.columns.length should equal(2)

    val (col1, col2) = (tbl.columns.head, tbl.columns(1))
    col1.name should equal("id")
    col1.columnType should equal(ColumnType.Integer)
    col2.name should equal("name")
    col2.columnType should equal(ColumnType.String)
  }

  private def createSampleTable(
    url: String, username: Option[String] = None, password: Option[String] = None): Unit = {
    val connection = DriverManager.getConnection(url, username.orNull, password.orNull)

    try {
      val statement = connection.createStatement()
      statement.executeUpdate("DROP TABLE IF EXISTS sample")
      statement.executeUpdate("CREATE TABLE sample (id INT PRIMARY KEY, name VARCHAR(30));")
      statement.executeUpdate("INSERT INTO sample (id, name) VALUES (1, 'sample');")
      statement.close()
    } finally {
      connection.close()
    }
  }

  private def getFilesInDirectory(path: File): Seq[File] = {
    path.listFiles.filter(_.isDirectory).flatMap(getFilesInDirectory) ++ path.listFiles
  }
}
