package rdb2rdf.translator

import rdb2rdf.graph.{DatabaseGraph, MappingGraph}

trait DatabaseGraphToMappingGraphTranslator {

  var databaseGraph: Option[DatabaseGraph] = None

  def setDatabaseGraph(databaseGraph: DatabaseGraph) = {
    this.databaseGraph = Some(databaseGraph)
    this
  }

  def translate: Option[MappingGraph]
}
