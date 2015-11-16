package rdb2rdf.database.representation

import java.net.URI
import java.sql.Types

object DataType {
  private val xsdIRI = "http://www.w3.org/2001/XMLSchema#"
  private val rdfIRI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"

  val XSD_STRING_IRI = new URI(xsdIRI + "string")
  val RDF_LITERAL_IRI = new URI(rdfIRI + "PlainLiteral")
  val XSD_BOOLEAN_IRI = new URI(xsdIRI + "boolean")
  val XSD_BYTE_IRI = new URI(xsdIRI + "byte")
  val XSD_LONG_IRI = new URI(xsdIRI + "long")
  val XSD_HEXBINARY_IRI = new URI(xsdIRI + "hexBinary")
  val XSD_DECIMAL_IRI = new URI(xsdIRI + "decimal")
  val XSD_INT_IRI = new URI(xsdIRI + "int")
  val XSD_SHORT_IRI = new URI(xsdIRI + "short")
  val XSD_FLOAT_IRI = new URI(xsdIRI + "float")
  val XSD_DOUBLE_IRI = new URI(xsdIRI + "double")
  val XSD_DATE_IRI = new URI(xsdIRI + "date")
  val XSD_TIME_IRI = new URI(xsdIRI + "time")
  val XSD_DATETIME_IRI = new URI(xsdIRI + "datetime")

  def buildValueTypeURI(typeNumber: Int): URI = typeNumber match {
    case Types.LONGNVARCHAR => XSD_STRING_IRI
    case Types.NCHAR => XSD_STRING_IRI
    case Types.NVARCHAR => XSD_STRING_IRI
    case Types.ROWID => RDF_LITERAL_IRI
    case Types.BIT => XSD_BOOLEAN_IRI
    case Types.TINYINT => XSD_BYTE_IRI
    case Types.BIGINT => XSD_LONG_IRI
    case Types.LONGVARBINARY => XSD_STRING_IRI
    case Types.VARBINARY => XSD_HEXBINARY_IRI
    case Types.BINARY => XSD_HEXBINARY_IRI
    case Types.LONGVARCHAR => XSD_STRING_IRI
    case Types.NULL => RDF_LITERAL_IRI
    case Types.CHAR => XSD_STRING_IRI
    case Types.NUMERIC => XSD_DECIMAL_IRI
    case Types.DECIMAL => XSD_DECIMAL_IRI
    case Types.INTEGER => XSD_INT_IRI
    case Types.SMALLINT => XSD_SHORT_IRI
    case Types.FLOAT => XSD_FLOAT_IRI
    case Types.REAL => XSD_FLOAT_IRI
    case Types.DOUBLE => XSD_DOUBLE_IRI
    case Types.VARCHAR => XSD_STRING_IRI
    case Types.BOOLEAN => XSD_BOOLEAN_IRI
    case Types.DATALINK => RDF_LITERAL_IRI
    case Types.DATE => XSD_DATE_IRI
    case Types.TIME => XSD_TIME_IRI
    case Types.TIMESTAMP => XSD_DATETIME_IRI
    case Types.OTHER => RDF_LITERAL_IRI
    case Types.JAVA_OBJECT => RDF_LITERAL_IRI
    case Types.DISTINCT => RDF_LITERAL_IRI
    case Types.STRUCT => RDF_LITERAL_IRI
    case Types.ARRAY => RDF_LITERAL_IRI
    case Types.BLOB => XSD_HEXBINARY_IRI
    case Types.CLOB => RDF_LITERAL_IRI
    case Types.REF => RDF_LITERAL_IRI
    case Types.SQLXML => RDF_LITERAL_IRI
    case Types.NCLOB => RDF_LITERAL_IRI
    case _ => throw new IllegalArgumentException("This type is not supported!")
  }
}
