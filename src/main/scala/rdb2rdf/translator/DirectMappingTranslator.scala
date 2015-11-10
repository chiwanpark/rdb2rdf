package rdb2rdf.translator

import rdb2rdf.graph.MappingGraph
import rdb2rdf.graph.MappingGraph._

class DirectMappingTranslator extends DatabaseGraphToMappingGraphTranslator {
  override def translate(): Option[MappingGraph] = databaseGraph match {
    case Some(dbGraph) =>
      val entityVertices = dbGraph.getAllTables.map(v => EntityVertex(v, dbGraph.getPrimaryKey(v).get))
      val columnEdges = entityVertices.flatMap { entity =>
        dbGraph.getColumns(entity.table).map { c => (entity, EntityValueEdge(c), ValueVertex(c)) }
      }
      val valueVertices = columnEdges.map(_._3)
      val foreignKeyEdges = entityVertices.flatMap { entity =>
        dbGraph.getForeignKeys(entity.table).map { c =>
          val linkedColumn = dbGraph.getLinkedColumnWithForeignKey(c).get
          (entity, OneToManyEntityEdge(c), EntityVertex(linkedColumn.table, linkedColumn))
        }
      }

      Some(new MappingGraph(entityVertices ++ valueVertices, columnEdges ++ foreignKeyEdges))
    case None => None
  }
}
