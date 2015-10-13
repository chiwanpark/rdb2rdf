package rdb2rdf.ui.event

import scala.swing.Dialog
import scala.swing.event.Event

case class CloseDialog(sender: Dialog) extends Event
