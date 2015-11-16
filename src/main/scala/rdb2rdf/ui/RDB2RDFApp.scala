package rdb2rdf.ui

import scala.swing._

object RDB2RDFApp extends SimpleSwingApplication {
  System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer")

  val appFrame = new AppFrame
  val controller = new Controller(appFrame)

  override def top: Frame = appFrame
}
