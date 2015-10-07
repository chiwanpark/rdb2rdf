package rdb2rdf.graph

import org.scalatest.{FlatSpec, Matchers}

class DirectGraphSpec extends FlatSpec with Matchers {
  behavior of "DirectGraph"

  val vertices = Set("a", "b", "c")
  val edges = Set(
    ("a", "linked", "b"),
    ("b", "linked", "a"),
    ("a", "highly-linked", "c"),
    ("c", "highly-linked", "c")
  )
  val graph = new DirectGraph(vertices, edges)

  it can "store vertices and edges" in {
    graph.vertices should equal(vertices)
    graph.edges should equal(edges)
  }

  it can "fetch connected vertices" in {
    graph.connectedVertices("a") should equal(Set("b", "c"))
  }

  it can "fetch connected edges" in {
    graph.connectedEdges("b") should equal(Set(("b", "linked", "a")))
  }

  it can "fetch connected vertices with edge filter" in {
    graph.connectedVerticesWithEdgeFilter("a")(_ == "highly-linked") should contain("c")
  }
}
