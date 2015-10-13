package rdb2rdf.ui

import java.awt.Dimension

import rdb2rdf.ui.event.{ExitProgram, OpenAddDatabaseDialog}
import rdb2rdf.ui.panel.GraphPanel

import scala.swing.event.ButtonClicked
import scala.swing.{BorderPanel, ToolBar, Button, Frame}

class AppFrame extends Frame {
  title = "RDB2RDF"
  preferredSize = new Dimension(640, 480)

  centerOnScreen()

  val addDatabaseButton = new Button("Add database")
  val clearGraphButton = new Button("Clear graph")
  val exitButton = new Button("Exit")

  val toolbar = new ToolBar {
    contents ++= Seq(addDatabaseButton, clearGraphButton, exitButton)
  }

  val graphPanel = new GraphPanel

  contents = new BorderPanel {
    layout(toolbar) = BorderPanel.Position.North
    layout(graphPanel) = BorderPanel.Position.Center
  }

  listenTo(addDatabaseButton, clearGraphButton, exitButton)
  reactions += {
    case ButtonClicked(comp) if comp == addDatabaseButton =>
      publish(OpenAddDatabaseDialog(this))
    case ButtonClicked(comp) if comp == clearGraphButton =>
      graphPanel.clearGraph()
    case ButtonClicked(comp) if comp == exitButton =>
      publish(ExitProgram(this))
  }
}
