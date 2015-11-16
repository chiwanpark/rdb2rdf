package rdb2rdf.database.representation

@SerialVersionUID(1L)
case class Constraint(
  column: Column,
  constraint: Constraint.Description.Type,
  optionalInfo: Option[String]) extends Serializable {

}

object Constraint {

  object Description extends Enumeration {
    type Type = Value

    val PrimaryKey, ForeignKey = Value
  }

}
