package rdb2rdf.graph

import rdb2rdf.graph.DatabaseGraph.{ColumnVertex, TableVertex}
import rdb2rdf.graph.MappingGraph.{Edge, Vertex}

class MappingGraph(
  vertexes: Set[Vertex],
  edges: Set[(Vertex, Edge, Vertex)]) extends DirectGraph(vertexes, edges)

object MappingGraph {

  abstract class Vertex

  abstract class Edge

  case class EntityVertex(
    table: TableVertex,
    keyColumn: ColumnVertex) extends Vertex

  case class ValueVertex(
    column: ColumnVertex) extends Vertex

  case class EntityValueEdge(
    column: ColumnVertex) extends Edge

  case class OneToManyEntityEdge(
    sourceColumn: ColumnVertex) extends Edge

  case class ManyToManyEntityEdge(
    mappingTable: TableVertex,
    leftColumn: ColumnVertex,
    rightColumn: ColumnVertex) extends Edge

}
