package rdb2rdf.graph

class DirectGraph[V, E](
  val vertices: Set[V],
  val edges: Set[(V, E, V)]) {

  def connectedVertices(v: V): Set[V] = edges.filter(_._1 == v).map(_._3)

  def connectedEdges(v: V): Set[(V, E, V)] = edges.filter(_._1 == v)

  def connectedVerticesWithEdgeFilter(v: V)(f: (E) => Boolean): Set[V] =
    connectedEdges(v).filter { case (v1, e, v2) => f(e) }.map(_._3)
}
