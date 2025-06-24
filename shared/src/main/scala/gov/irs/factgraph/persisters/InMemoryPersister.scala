package gov.irs.factgraph.persisters

import gov.irs.factgraph.Fact
import gov.irs.factgraph.Path
import gov.irs.factgraph.Migrations
import gov.irs.factgraph.PersisterSyncIssue
import gov.irs.factgraph.types._
import gov.irs.factgraph.definitions.fact.LimitLevel
import gov.irs.factgraph.limits.LimitViolation
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.compnodes.{CompNode, BooleanNode, DollarNode, IntNode, StringNode}

import scala.collection.mutable
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import upickle.default.{write, read}
import gov.irs.factgraph.Graph
import gov.irs.factgraph.FactDictionary
import ujson.Value

import java.util.UUID;

final class InMemoryPersister(var store: Map[Path, WritableType]) extends Persister:
  private val live = mutable.Map[Path, WritableType]() ++= store
  private val toValidate = mutable.Queue[Fact]()
  // this may need to be changed into an actual FSM at some point if it gets more complicated
  private var validating = false
  private var validationStore: Map[Path, WritableType] = store
  override def getSavedResult[A](path: Path, klass: Class[A]): Result[A] =
    val st = if validating then validationStore else store
    st.get(path) match
      case Some(value) => Result.Complete(value.asInstanceOf[A])
      case None        => Result.Incomplete

  override def setFact(fact: Fact, value: WritableType): Unit =
    require(fact.path.isKnown)

    live(fact.path) = value
    toValidate += fact

  /** Delete a fact from this persister.
    *
    * @param fact
    *   The fact to delete.
    * @param deleteSubpaths
    *   Whether to cascade the delete (e.g., `fact.path` is for a collection item).
    */
  override def deleteFact(fact: Fact, deleteSubpaths: Boolean = false): Unit =
    require(fact.path.isKnown)
    // If `toValidate` contains this fact, we need to delete that fact from `toValidate`.
    // Otherwise, our limit validator will attempt to fetch the result of a deleted fact.
    toValidate.dequeueAll(f => f.path == fact.path)
    deleteSubpaths match
      case true =>
        for ((path, writeable) <- live) do
          path.toString.startsWith(
            s"${fact.path.toString}${Path.Delimiter}",
          ) match
            case true  => fact.graph.delete(path)
            case false =>
      case false =>
        live -= fact.path

  private def pathHasBadItemId(
      path: Path,
      graph: Graph,
  ) =
    path.getMemberId match
      case None => false
      case Some(id) =>
        !graph
          .getCollectionPaths(path.asAbstract.toString)
          .exists(collectionPath => collectionPath.contains(id.toString))

  private def pathExistsAndIsWritable(
      path: Path,
      dictionary: FactDictionary,
  ) =
    dictionary
      .getPaths()
      .exists(dictionaryPath =>
        dictionaryPath == path && dictionary(
          dictionaryPath,
        ).get.isWritable,
      )

  private def checkTypeMapping(
      writableType: WritableType,
      dictionaryType: Class[? <: CompNode],
  ) =
    val persistedValString = writableType.toString()
    val dictionaryTypeName = dictionaryType.getSimpleName()
    writableType match
      case _: java.lang.String if (dictionaryType != classOf[StringNode]) =>
        (
          false,
          Some(
            s"Removed String type value persisted with ${dictionaryTypeName} path",
          ),
        )
      case _: java.lang.Byte if dictionaryType != classOf[IntNode] =>
        (
          false,
          Some(
            s"Removed Byte type value persisted with ${dictionaryTypeName} path",
          ),
        )
      case _: java.lang.Short if dictionaryType != classOf[IntNode] =>
        (
          false,
          Some(
            s"Removed Short type value persisted with ${dictionaryTypeName} path",
          ),
        )
      case _: java.lang.Integer if dictionaryType != classOf[IntNode] =>
        (
          false,
          Some(
            s"Removed Integer type value persisted with ${dictionaryTypeName} path",
          ),
        )
      case _: java.lang.Boolean if dictionaryType != classOf[BooleanNode] =>
        (
          false,
          Some(
            s"Removed Boolean type value persisted with ${dictionaryTypeName} path",
          ),
        )
      case _: scala.math.BigDecimal if dictionaryType != classOf[DollarNode] =>
        (
          false,
          Some(
            s"Removed BigDecimal type value persisted with ${dictionaryTypeName} path",
          ),
        )
      case _ =>
        (true, None)

  override def syncWithDictionary(graph: Graph): Seq[PersisterSyncIssue] =
    val results = live
      .map((path, writableType) => {
        val dictionaryPath = path.isCollectionMember match
          case true  => path.asAbstract
          case false => path

        val (keepFact, optionalMessage) =
          if (pathHasBadItemId(path, graph))
            (false, Some("Removed bad item ID path from persister"))
          else if (!pathExistsAndIsWritable(dictionaryPath, graph.dictionary))
            (false, Some("Removed unknown or non-writable path from persister"))
          else
            writableType match
              case _: WritableType =>
                checkTypeMapping(
                  writableType,
                  graph.dictionary(dictionaryPath).get.value.getClass,
                )

        if (keepFact)
          val toWrite = writableType match
            case _ => writableType
          graph.set(path, toWrite)
          if (optionalMessage.nonEmpty)
            Some(PersisterSyncIssue(path.toString, optionalMessage.get))
          else None
        else
          live -= path
          Some(
            PersisterSyncIssue(
              path.toString,
              optionalMessage.getOrElse("Description is missing for this error"),
            ),
          )
      })
      .flatten
      .toSeq

    save()
    results

  override def save(): (Boolean, Seq[LimitViolation]) =
    validationStore = live.toMap
    validating = true
    val issues = toValidate.flatMap(x => x.validate()).toSeq
    toValidate.clear()
    validating = false
    // The only level that should stop us from saving
    // is an actual error
    if (issues.exists(x => x.LimitLevel == LimitLevel.Error)) (false, issues)
    else
      store = live.toMap
      (true, issues)

  // toStringMap is a purely debug function for just inspecting your graph
  def toStringMap(): Map[String, String] =
    store
      .map((path, value) => (path.toString, s"${value.toString}"))

  def toJson(indent: Int = -1): String =
    // Because migrations are applied prior to initializing the InMemoryPersister,
    // all in-memory persisters are guaranteed to have all the most recent migrations.
    // Therefore, upon serializing it, we simply add the current number of migrations;
    // when it's deserialized into a possibly-new Persister, any new migrations will be applied.
    val wrappedStore = store
      .map((path, value) => (path.toString, TypeContainer(value)))
      + (Migrations.MigrationsFieldName -> TypeContainer(Migrations.TotalMigrations))
    write(wrappedStore, indent)

object InMemoryPersister:
  def apply(): InMemoryPersister = new InMemoryPersister(
    Map[Path, WritableType](),
  )

  def apply(seeds: (String, WritableType)*): InMemoryPersister =
    val pathSeeds = seeds.map((path, value) => (Path(path), value))
    new InMemoryPersister(Map(pathSeeds*))

  def apply(jsonString: String): InMemoryPersister =
    val jsonData = read[Map[String, Value]](jsonString)

    // Migrations are applied only to fact graphs that were persisted via JSON string;
    // everything else is initialized in-memory, and therefore assumed to be current
    val totalMigrations = jsonData
      .get(Migrations.MigrationsFieldName)
      .flatMap(data => data("item").numOpt)
      .getOrElse(0.0)
      .toInt

    // The migrations field is not actually stored in-memory (it only appears on serialization)
    // so we drop it before converting the JSON to actual facts
    val unwrappedData = jsonData.-(Migrations.MigrationsFieldName)
    val migratedData = Migrations.run(unwrappedData, totalMigrations)

    new InMemoryPersister(migratedData)
