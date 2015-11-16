package rdb2rdf.database.representation

@SerialVersionUID(1L)
case class Database(
  jdbcUrl: String,
  username: Option[String],
  password: Option[String],
  tables: Set[Table]) extends Serializable {

}
