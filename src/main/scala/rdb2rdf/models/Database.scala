package rdb2rdf.models

import rdb2rdf.models.ColumnType.ColumnType

/** Column type enumeration */
object ColumnType extends Enumeration {
  type ColumnType = Value

  val Boolean, Integer, Real, String, DateTime, Blob = Value

  /** Returns [[rdb2rdf.models.ColumnType.ColumnType]] object for given SQL type index.
    *
    * @param index Index of SQL types
    * @return [[rdb2rdf.models.ColumnType.ColumnType]] which is matched for given SQL type index
    */
  def fromSqlType(index: Int): ColumnType = index match {
    case i if i == -7 => Boolean
    case i if Seq(-6, 5, 4, -5) contains i => Integer
    case i if Seq(2, 3, 6, 7, 8) contains i => Real
    case i if Seq(1, 12, -1) contains i => String
    case i if Seq(91, 92, 93) contains i => DateTime
    case _ => Blob
  }
}

/** Representation of Database
  *
  * @param url URL of database
  * @param tables Sequence of tables in the database
  */
case class Database(url: String, tables: Seq[DatabaseTable]) {
  override def toString: String = s"Database(url: $url)"
}

/** Representation of Database Table
  *
  * @param tableName Name of database table
  * @param columns Sequence of columns in the table
  */
case class DatabaseTable(tableName: String, columns: Seq[DatabaseColumn]) {
  override def toString: String = s"DatabaseTable(tableName: $tableName)"
}

/** Representation of Database Column
  *
  * @param name Name of column
  * @param columnType Type of column
  */
case class DatabaseColumn(name: String, columnType: ColumnType) {
  override def toString: String = s"DatabaseColumn(name: $name, columnType: $columnType)"
}
