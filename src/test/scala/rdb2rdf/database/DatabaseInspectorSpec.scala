package rdb2rdf.database

import java.io.File
import java.nio.file.Files
import java.sql.{DriverManager, SQLException}

import org.scalatest.{FlatSpec, Matchers}
import org.slf4j.LoggerFactory
import rdb2rdf.database.representation.Constraint

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

    val inspector = new DatabaseInspector(url)
    val connection = inspector.createJdbcConnection()
    try {
      checkTables(inspector.getTables(connection), checkUpper = true)
      checkColumns(inspector.getColumns(connection), checkUpper = true)
      checkPrimaryKeys(inspector.getPrimaryKeys(connection), checkUpper = true)
      checkForeignKeys(inspector.getForeignKeys(connection), checkUpper = true)
      checkInspect(inspector, checkUpper = true)
    } finally {
      connection.close()
    }
  }

  it should "can inspect PostgreSQL database" in {
    val url = "jdbc:postgresql://localhost:5432/travis_ci_test"
    try {
      createSampleTable(url, Some("postgres"))

      val inspector = new DatabaseInspector(url)
      val connection = inspector.createJdbcConnection()
      try {
        checkTables(inspector.getTables(connection))
        checkColumns(inspector.getColumns(connection))
        checkPrimaryKeys(inspector.getPrimaryKeys(connection))
        checkForeignKeys(inspector.getForeignKeys(connection))
        checkInspect(inspector)
      } finally {
        connection.close()
      }
    } catch {
      case e: SQLException if e.toString contains "refused" =>
        LOG.warn("Test for PostgreSQL is not executed. Please check PostgreSQL server.", e)
    }
  }

  it should "can inspect SQLite database" in {
    val url = "jdbc:sqlite:" + tempDir.toString + File.separator + "test_sqlite.db"
    createSampleTable(url)

    val inspector = new DatabaseInspector(url)
    val connection = inspector.createJdbcConnection()
    try {
      checkTables(inspector.getTables(connection), checkUpper = true)
      checkColumns(inspector.getColumns(connection), checkUpper = true)
      checkForeignKeys(inspector.getForeignKeys(connection), checkUpper = true)
      // TODO: fix uppercase/lowercase column name problem in primary key and foreign key constraints
      // checkInspect(inspector, checkPrimaryKeyConstraint = false, checkUpper = true)
    } finally {
      connection.close()
    }
  }

  it should "can inspect MySQL database" in {
    val url = "jdbc:mysql://localhost:3306/travis_ci_test"
    try {
      createSampleTable(url, Some("root"))

      val inspector = new DatabaseInspector(url, Some("root"))
      val connection = inspector.createJdbcConnection()
      try {
        checkTables(inspector.getTables(connection), checkUpper = true)
        checkColumns(inspector.getColumns(connection), checkUpper = true)
        checkPrimaryKeys(inspector.getPrimaryKeys(connection), checkUpper = true)
        checkForeignKeys(inspector.getForeignKeys(connection), checkUpper = true)
        checkInspect(inspector, checkUpper = true)
      } finally {
        connection.close()
      }
    } catch {
      case e: SQLException if e.toString contains "link failure" =>
        LOG.warn("Test for MySQL is not executed. Please check MySQL server.", e)
    }
  }

  private def checkInspect(
    inspector: DatabaseInspector,
    checkPrimaryKeyConstraint: Boolean = true,
    checkUpper: Boolean = false): Unit = {
    val database = inspector.inspect()

    val tables = database.tables.map(_.name).toSeq
    val columns = database.tables.flatMap { table =>
      table.columns.map(column => (table.name, column.name, column.columnType))
    }.toSeq

    val foreignKeys = database.tables.flatMap { table =>
      table.constraints.filter(_.constraint == Constraint.Description.ForeignKey).map {
        case Constraint(column, Constraint.Description.ForeignKey, Some(optionalInfo: String)) =>
          val split = optionalInfo.split("/")
          ((table.name, column.name), (split(0), split(1)))
      }
    }.toMap
    checkForeignKeys(foreignKeys, checkUpper)

    checkTables(tables, checkUpper)
    checkColumns(columns, checkUpper)

    if (checkPrimaryKeyConstraint) {
      val primaryKeys = database.tables.flatMap { table =>
        table.constraints.filter(_.constraint == Constraint.Description.PrimaryKey).map {
          case Constraint(column, Constraint.Description.PrimaryKey, _) =>
            (table.name, column.name)
        }
      }.toSeq
      checkPrimaryKeys(primaryKeys, checkUpper)
    }
  }

  private def checkTables(givenTables: Seq[String], checkUpper: Boolean = false): Unit = {
    val names = Seq("sample1", "sample2")

    names.map { name => if (checkUpper) name.toUpperCase else name }
      .forall(givenTables.contains(_)) should equal(true)
  }

  private def checkColumns(givenColumns: Seq[(String, String, Int)], checkUpper: Boolean = false): Unit = {
    val tables = Seq("sample1", "sample2")
    val columns = Map("sample1" -> Seq("id", "name"), "sample2" -> Seq("id", "name", "sample1_id"))

    val tblColPairs = for {
      table <- tables
      column <- columns(table)
    } yield if (checkUpper) {
      (table.toUpperCase, column.toUpperCase)
    } else {
      (table, column)
    }

    tblColPairs.forall { case (table, column) =>
      val dataType = if (column.contains("id") || column.contains("ID")) 4 else 12
      givenColumns.contains((table, column, dataType))
    } should equal(true)
  }

  private def checkPrimaryKeys(givenPks: Seq[(String, String)], checkUpper: Boolean = false): Unit = {
    val tables = Seq("sample1", "sample2")

    tables.map { table => if (checkUpper) (table.toUpperCase, "ID") else (table, "id") }
      .forall(givenPks.contains(_)) should equal(true)
  }

  private def checkForeignKeys(
    givenFks: Map[(String, String), (String, String)],
    checkUpper: Boolean = false): Unit = {

    val foreignKeys = if (checkUpper) {
      Map(("SAMPLE2", "SAMPLE1_ID") ->("SAMPLE1", "ID"))
    } else {
      Map(("sample2", "sample1_id") ->("sample1", "id"))
    }

    givenFks should equal(foreignKeys)
  }

  private def createSampleTable(
    url: String,
    username: Option[String] = None,
    password: Option[String] = None): Unit = {

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
