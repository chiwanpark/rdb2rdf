package rdb2rdf.models

import java.net.URI

/** Representation of RDF Graph
  *
  * @param subject URI of subject
  * @param predicate URI of predicate
  * @param target URI of target (object)
  */
case class RDFGraph(subject: URI, predicate: URI, target: URI) {
  override def toString: String = s"RDFGraph(subject: $subject, predicate: $predicate, target: $target)"
}

/** Representation of relationship between columns
  *
  * @param subjectColumn Subject column
  * @param predicate URI of predicate
  * @param targetColumn Target column (object)
  */
case class ColumnMapping(subjectColumn: DatabaseColumn, predicate: URI, targetColumn: DatabaseColumn) {
  override def toString: String =
    s"ColumnMapping(subject: $subjectColumn, predicate: $predicate, target: $targetColumn"
}
