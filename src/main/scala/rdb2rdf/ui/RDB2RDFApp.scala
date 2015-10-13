package rdb2rdf.ui

import scala.swing._

object RDB2RDFApp extends SimpleSwingApplication {
  val appFrame = new AppFrame
  val controller = new Controller(appFrame)

  override def top: Frame = appFrame
}
