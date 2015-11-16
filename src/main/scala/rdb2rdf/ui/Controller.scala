package rdb2rdf.ui

import org.slf4j.LoggerFactory
import rdb2rdf.database.DatabaseInspector
import rdb2rdf.database.representation.Database
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
      val inspectDBOperation = Future[Database] {
        new DatabaseInspector(jdbcUrl, usernameOpt, passwordOpt).inspect()
      }

      inspectDBOperation onSuccess { case result =>
        Swing.onEDT {
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

    case _ =>
  }
}
