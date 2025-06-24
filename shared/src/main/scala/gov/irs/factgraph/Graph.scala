package gov.irs.factgraph

import gov.irs.factgraph.limits.LimitViolation
import gov.irs.factgraph.monads.*
import gov.irs.factgraph.persisters.*
import gov.irs.factgraph.types.{Collection, CollectionItem, WritableType, Enum}
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.collection.mutable
import scala.scalajs.js.annotation.JSExportAll

class Graph(val dictionary: FactDictionary, val persister: Persister):
  val root: Fact = Fact(this)

  private[factgraph] val factCache = mutable.HashMap[Path, Option[Fact]]()
  private[factgraph] val resultCache =
    mutable.HashMap[Path, MaybeVector[Result[Any]]]()

  export root.apply

  @JSExport("get")
  def get(path: String): Result[Any] = get(Path(path))

  @JSExport("getWithPath")
  def get(path: Path): Result[Any] = getVect(path) match
    case MaybeVector.Single(result) => result
    case MaybeVector.Multiple(_, _) =>
      throw new UnsupportedOperationException(
        s"must use getVect to access '$path'",
      )

  @JSExport("getVect")
  def getVect(path: String): MaybeVector[Result[Any]] = getVect(Path(path))
  @JSExport("getVectWithPath")
  def getVect(path: Path): MaybeVector[Result[Any]] =
    for {
      fact <- this(path)
      values <- fact match
        case Result(fact, complete) =>
          if (complete) fact.get
          else fact.get.map(_.asPlaceholder)
        case _ =>
          throw new UnsupportedOperationException(
            s"path '$path' was not found",
          )
    } yield values

  @JSExport("explain")
  def explain(path: String): Explanation = explain(Path(path))
  @JSExport("explainWithPath")
  def explain(path: Path): Explanation =
    val explanations = for {
      fact <- this(path)
      explanation <- fact match
        // Note that we are discarding the completeness of the path's
        // resolution; we are providing an explanation of a *fact's* result,
        // not why a particular *path* returns a potentially incomplete result.
        case Result(fact, _) =>
          fact.explain
        case _ =>
          throw new UnsupportedOperationException(
            s"path '$path' was not found",
          )
    } yield explanation

    explanations match
      case MaybeVector.Single(explanation) => explanation
      case MaybeVector.Multiple(_, _) =>
        throw new UnsupportedOperationException(
          s"path '$path' resolves to a vector",
        )

  @JSExport("set")
  def set(path: String, value: WritableType): Unit = set(Path(path), value)
  @JSExport("setWithPath")
  def set(path: Path, value: WritableType): Unit =
    for {
      result <- this(path)
      fact <- result
    } fact.set(value)

  @JSExport("delete")
  def delete(path: String): Unit = delete(Path(path))
  @JSExport("deleteWithPath")
  def delete(path: Path): Unit =
    for {
      result <- this(path)
      fact <- result
    } fact.delete()

  def checkPersister(): Seq[PersisterSyncIssue] =
    persister.syncWithDictionary(this)

  def save(): (Boolean, Seq[LimitViolation]) =
    factCache.clear()
    resultCache.clear()

    val out = persister.save()

    // Don't cache invalid results
    if !out._1 then resultCache.clear()

    out

  @JSExport("getDictionary")
  def getDictionary() = this.dictionary

  def getCollectionPaths(collectionPath: String): Seq[String] =
    val paths = for
      pathPerhapsWithWildcards <- Path(collectionPath).populateWildcards(this)
      pathWithoutWildcards <- pathPerhapsWithWildcards.populateWildcards(this)
    yield pathWithoutWildcards.toString

    paths.toSeq

object Graph:
  def apply(dictionary: FactDictionary): Graph =
    this(dictionary, InMemoryPersister())

  def apply(dictionary: FactDictionary, persister: Persister): Graph =
    new Graph(dictionary, persister)
