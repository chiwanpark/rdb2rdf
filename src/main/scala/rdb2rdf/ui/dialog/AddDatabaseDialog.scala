package rdb2rdf.ui.dialog

import rdb2rdf.ui.event.{CloseDialog, RequestAddDatabase}

import scala.swing._
import scala.swing.event.ButtonClicked

class AddDatabaseDialog(frame: Frame) extends Dialog(frame) with Publisher {
  title = "Add database"
  modal = true
  resizable = false

  setLocationRelativeTo(frame)

  val jdbcUrl = new TextField
  val dbName = new TextField()
  val username = new TextField
  val password = new PasswordField
  val addButton = new Button("Add")
  val cancelButton = new Button("Cancel")

  val infoPanel = new BoxPanel(Orientation.Vertical) {
    contents ++= Seq(new Label("JDBC URL"), jdbcUrl)
    contents ++= Seq(new Label("Database name (For identifying)"), dbName)
    contents ++= Seq(new Label("Username"), username)
    contents ++= Seq(new Label("Password"), password)

    border = Swing.EmptyBorder(10)
  }

  val buttonPanel = new FlowPanel(FlowPanel.Alignment.Right)(addButton, cancelButton) {
    border = Swing.EmptyBorder(5)
  }

  contents = new BorderPanel {
    layout(infoPanel) = BorderPanel.Position.North
    layout(buttonPanel) = BorderPanel.Position.South
  }

  listenTo(addButton, cancelButton)
  reactions += {
    case ButtonClicked(comp) if comp == addButton =>
      val dbNameOpt = if (dbName.text != "") Some(dbName.text) else None
      val usernameOpt = if (username.text != "") Some(username.text) else None
      val passwordOpt = if (!password.password.isEmpty) Some(password.password.mkString("")) else None

      publish(RequestAddDatabase(this, jdbcUrl.text, dbNameOpt, usernameOpt, passwordOpt))
    case ButtonClicked(comp) if comp == cancelButton =>
      publish(CloseDialog(this))
  }
}
