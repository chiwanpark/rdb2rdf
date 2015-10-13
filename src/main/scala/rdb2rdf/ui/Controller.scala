package rdb2rdf.ui

import org.slf4j.LoggerFactory
import rdb2rdf.database.DatabaseInspector
import rdb2rdf.graph.DatabaseGraph
import rdb2rdf.ui.dialog.AddDatabaseDialog
import rdb2rdf.ui.event.{CloseDialog, ExitProgram, OpenAddDatabaseDialog, RequestAddDatabase}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.swing.{Dialog, Publisher, Swing}

class Controller(appFrame: AppFrame) extends Publisher {
  val LOG = LoggerFactory.getLogger(this.getClass)

  listenTo(appFrame)

  reactions += {
    case RequestAddDatabase(sender, jdbcUrl, dbNameOpt, usernameOpt, passwordOpt) =>
      val inspectDBOperation = Future[DatabaseGraph] {
        val inspector = new DatabaseInspector(jdbcUrl, usernameOpt, passwordOpt)
        val connection = inspector.getJdbcConnection
        val builder = new DatabaseGraph.Builder()
          .setDatabaseJdbcUrl(jdbcUrl)
          .setColumns(inspector.getColumns(connection))
          .setPrimaryKeys(inspector.getPrimaryKeys(connection))
          .setForeignKeys(inspector.getForeignKeys(connection))
          .setTables(inspector.getTables(connection))

        dbNameOpt match {
          case Some(name) => builder.setDatabaseName(name)
          case None =>
        }

        builder.build()
      }

      inspectDBOperation onSuccess { case result =>
        Swing.onEDT {
          appFrame.graphPanel.addDatabaseGraph(result)
        }
      }

      inspectDBOperation onFailure { case e =>
        LOG.error("Failure on inspecting database!", e)
        Swing.onEDT {
          Dialog.showMessage(
            appFrame, "Cannot inspect database from given connection information!", "Error", Dialog.Message.Error)
        }
      }

      deafTo(sender)
      sender.close()

    case OpenAddDatabaseDialog(sender) =>
      val dialog = new AddDatabaseDialog(sender)
      listenTo(dialog)
      dialog.open()

    case CloseDialog(sender) =>
      deafTo(sender)
      sender.close()

    case ExitProgram(sender) =>
      sys.exit(0)
  }
}
