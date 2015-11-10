package rdb2rdf.graph

import rdb2rdf.graph.DatabaseGraph.{ColumnVertex, TableVertex}
import rdb2rdf.graph.MappingGraph.{Edge, Vertex}

class MappingGraph(
  vertices: Set[Vertex],
  edges: Set[(Vertex, Edge, Vertex)]) extends DirectGraph(vertices, edges)

object MappingGraph {

  abstract class Vertex {
    def identifier: String
  }

  abstract class Edge {
    def identifier: String
  }

  case class EntityVertex(
    table: TableVertex,
    keyColumn: ColumnVertex) extends Vertex {
    override def identifier: String = s"${table.identifier}#${keyColumn.simpleIdentifier}"
  }

  case class ValueVertex(
    column: ColumnVertex) extends Vertex {
    override def identifier: String = s"${column.identifier}+{VALUE}"
  }

  case class EntityValueEdge(
    column: ColumnVertex) extends Edge {
    override def identifier: String = column.identifier
  }

  case class OneToManyEntityEdge(
    sourceColumn: ColumnVertex) extends Edge {
    override def identifier: String = s"${sourceColumn.table.identifier}#ref-${sourceColumn.simpleIdentifier}"
  }

  case class ManyToManyEntityEdge(
    mappingTable: TableVertex,
    leftColumn: ColumnVertex,
    rightColumn: ColumnVertex) extends Edge {
    override def identifier: String =
      s"${mappingTable.identifier}:${leftColumn.simpleIdentifier}:${rightColumn.simpleIdentifier}"
  }

}
