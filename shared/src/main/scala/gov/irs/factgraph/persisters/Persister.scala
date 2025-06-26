package gov.irs.factgraph.persisters

import gov.irs.factgraph.limits.LimitViolation
import gov.irs.factgraph.{Graph, Fact, Path, PersisterSyncIssue}
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.WritableType

trait Persister:
  def getSavedResult[A](path: Path, klass: Class[A]): Result[A]

  def setFact(fact: Fact, value: WritableType): Unit
  def deleteFact(fact: Fact, deleteSubpaths: Boolean = false): Unit

  def syncWithDictionary(graph: Graph): Seq[PersisterSyncIssue]
  def save(): (Boolean, Seq[LimitViolation])

  def toStringMap(): Map[String, String]
  def toJson(indent: Int = -1): String
