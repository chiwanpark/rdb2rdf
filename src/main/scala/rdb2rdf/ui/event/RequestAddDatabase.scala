package rdb2rdf.ui.event

import scala.swing.Dialog
import scala.swing.event.Event

case class RequestAddDatabase(
  sender: Dialog,
  jdbcUrl: String,
  name: Option[String],
  username: Option[String],
  password: Option[String]) extends Event
