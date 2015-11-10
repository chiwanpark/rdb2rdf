package rdb2rdf.translator

import org.scalatest.{FlatSpec, Matchers}
import org.slf4j.LoggerFactory
import rdb2rdf.util.TestUtils

class DirectMappingTranslatorSpec extends FlatSpec with Matchers {
  behavior of "DirectMappingTranslator"

  val LOG = LoggerFactory.getLogger(this.getClass)

  it can "translate from DatabaseGraph which is simple to MappingGraph in direct mapping manner" in {
    val graph = TestUtils.createSimpleGraph()
    val translator = new DirectMappingTranslator().setDatabaseGraph(graph)

    translator.translate match {
      case Some(mappingGraph) =>
        // TODO: We need to verify in more detail manner.
        mappingGraph.vertices.size should equal(6)
        mappingGraph.edges.size should equal(5)
      case None => fail("Translation from simple graph is failed!")
    }
  }

  it can "translate from DatabaseGraph which is complex to MappingGraph in direct mapping manner" in {
    val graph = TestUtils.createComplexGraph()
    val translator = new DirectMappingTranslator().setDatabaseGraph(graph)

    translator.translate match {
      case Some(mappingGraph) =>
        // TODO: We need to verify in more detail manner.
        mappingGraph.vertices.size should equal(10)
        mappingGraph.edges.size should equal(9)
      case None => fail("Translation from complex graph is failed!")
    }
  }
}
