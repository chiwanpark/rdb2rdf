package rdb2rdf.ui.panel

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout
import com.mxgraph.layout.mxParallelEdgeLayout
import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.view.mxGraph
import rdb2rdf.graph.DatabaseGraph
import rdb2rdf.graph.DatabaseGraph.Edge

import scala.collection.mutable
import scala.swing.{BoxPanel, Orientation}

class GraphPanel extends BoxPanel(Orientation.Vertical) {
  val graph = new mxGraph()
  val parent = graph.getDefaultParent
  val graphComponent = new mxGraphComponent(graph)

  peer.add(graphComponent)

  def clearGraph(): Unit = {
    graph.getModel.beginUpdate()
    try {
      graph.removeCells(graph.getChildVertices(parent))
    } finally {
      graph.getModel.endUpdate()
    }
  }

  def addDatabaseGraph(databaseGraph: DatabaseGraph): Unit = {
    val verticesMap = mutable.Map[String, Object]()

    graph.getModel.beginUpdate()
    try {
      verticesMap ++= databaseGraph.getAllDatabases.map { v =>
        val name = v.name.getOrElse(v.jdbcUrl)
        (v.identifier, graph.insertVertex(parent, "", name, 0, 0, name.length * 10, 20))
      }

      verticesMap ++= databaseGraph.getAllDatabases.flatMap { db =>
        databaseGraph.getTables(db).map { table =>
          (table.identifier, graph.insertVertex(parent, "", table.name, 0, 0, table.name.length * 10, 20))
        }
      }

      verticesMap ++= databaseGraph.getAllDatabases.flatMap { db =>
        databaseGraph.getTables(db).flatMap { table =>
          databaseGraph.getColumns(table).map { column =>
            (column.identifier, graph.insertVertex(parent, "", column.name, 0, 0, column.name.length * 8, 20))
          }
        }
      }

      databaseGraph.edges.filter(_._2 != Edge.Belongs).foreach { edge =>
        graph.insertEdge(parent, "", edge._2.toString, verticesMap(edge._1.identifier), verticesMap(edge._3.identifier))
      }
    } finally {
      graph.getModel.endUpdate()
    }

    new mxHierarchicalLayout(graph).execute(parent)
    new mxParallelEdgeLayout(graph).execute(parent)
  }
}
