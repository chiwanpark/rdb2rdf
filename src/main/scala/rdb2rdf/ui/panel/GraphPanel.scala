package rdb2rdf.ui.panel

import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.ui.spriteManager.SpriteManager
import org.graphstream.ui.view.Viewer.ThreadingModel
import org.graphstream.ui.view.{Viewer, ViewerListener}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.swing.{BoxPanel, Orientation, Publisher}

class GraphPanel extends BoxPanel(Orientation.Vertical) with Publisher with ViewerListener {
  val LOG = LoggerFactory.getLogger(this.getClass)

  val graph = new SingleGraph("RDB2RDF-MappingGraph")
  graph.addAttribute("ui.quality")

  val spriteManager = new SpriteManager(graph)

  val viewer = new Viewer(graph, ThreadingModel.GRAPH_IN_ANOTHER_THREAD)
  val view = viewer.addDefaultView(false)
  var viewerEventPump = true

  private val fromViewer = viewer.newViewerPipe()
  fromViewer.addViewerListener(this)

  private val pumpFuture = Future[Unit] {
    while (viewerEventPump) {
      fromViewer.blockingPump()
    }
  }

  peer.add(view)

  def clearGraph(): Unit = {
    viewer.disableAutoLayout()
    graph.clear()
    viewer.enableAutoLayout()
  }

  override def buttonReleased(s: String): Unit = {
    LOG.info(s"buttonReleased: $s")
  }

  override def buttonPushed(s: String): Unit = {
    LOG.info(s"buttonPushed: $s")
  }

  override def viewClosed(s: String): Unit = {
    LOG.info(s"viewClosed: $s")
  }
}
