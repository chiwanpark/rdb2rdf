package rdb2rdf.database.representation

@SerialVersionUID(1L)
case class Column(
  name: String,
  columnType: Int) extends Serializable {

}
