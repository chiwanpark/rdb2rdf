package rdb2rdf.graph

import org.scalatest.{FlatSpec, Matchers}
import rdb2rdf.graph.DatabaseGraph.TableVertex
import rdb2rdf.util.TestUtils

class DatabaseGraphSpec extends FlatSpec with Matchers {
  behavior of "DatabaseGraph"

  it can "be merged with other graph" in {
    val graph1 = TestUtils.createSimpleGraph()
    val graph2 = TestUtils.createSimpleGraph("testdb2")
    val merged = graph1 ++ graph2

    merged.vertices.size should equal(graph1.vertices.size + graph2.vertices.size)
    merged.edges.size should equal(graph1.edges.size + graph2.edges.size)
  }

  it can "fetch all tables in the graph" in {
    val graph = TestUtils.createSimpleGraph()
    val names = Seq("table1", "table2")
    val tables = graph.getAllTables

    tables.forall(t => names.contains(t.name)) should equal(true)
  }

  it can "fetch columns in specific table" in {
    val graph = TestUtils.createSimpleGraph()
    val table1 = graph.getAllTables.filter(_.name == "table1").head
    val columns = graph.getColumns(table1)
    val names = Seq("id", "name")

    columns.forall(c => names.contains(c.name)) should equal(true)
  }

  it can "fetch foreign keys from given table" in {
    val graph = TestUtils.createSimpleGraph()

    val table2 = graph.getAllTables.filter(_.name == "table2").head
    val linkedTable = graph.getForeignKeys(table2).map {
      c => graph.getLinkedColumnWithForeignKey(c).get.table
    }.collectFirst { case v: TableVertex => v }.get


    linkedTable.name should equal("table1")
  }
}
