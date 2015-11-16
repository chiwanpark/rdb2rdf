package rdb2rdf.database.representation

@SerialVersionUID(1L)
case class Table(
  name: String,
  columns: Set[Column],
  constraints: Set[Constraint]) extends Serializable {

}
