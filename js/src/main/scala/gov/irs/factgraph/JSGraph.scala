package gov.irs.factgraph
import gov.irs.factgraph.Expression.Writable
import gov.irs.factgraph.persisters.*

import scala.scalajs.js.annotation.{JSExport, JSExportAll, JSExportTopLevel}
import scala.scalajs.js
import gov.irs.factgraph.limits.LimitViolation
import gov.irs.factgraph.monads.MaybeVector
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.WritableType
import js.JSConverters._
import gov.irs.factgraph.types.Dollar
import gov.irs.factgraph.compnodes.DollarNode
import gov.irs.factgraph.compnodes.EnumNode


@JSExportTopLevel("Graph")
@JSExportAll
class JSGraph(
    override val dictionary: FactDictionary,
    override val persister: Persister,
) extends Graph(dictionary, persister):

  def toStringDictionary(): js.Dictionary[String] =
    // This is a debug function to allow for quick inspection
    // of the graph
    this.persister.toStringMap().toJSDictionary

  // Get a fact definition from the fact dictionary
  // In scala this is graph.apply
  def getFact(path: String) =
    root.apply(Path(path)) match
      case MaybeVector.Single(x) =>
        x match
          case Result.Complete(v)    => v
          case Result.Placeholder(v) => v
          case Result.Incomplete     => null

      case MaybeVector.Multiple(vect, c) =>
        throw new UnsupportedOperationException(
          s"getFact returned multiple results for path $path, which is unsupported",
        )

  // In HTML, form value are always strings
  // This method simplifies the interface for facts so that the consumer of the fact graph only
  // has to supply a string: the fact graph will convert it to the appropriate type based on the
  // definition, or throw an exception if that type is incorrect.
  def set(path: String, value: String): Unit = {

    // Convert "true" and "false" to booleans
    var typedValue: WritableType = value match {
      case "true" => true
      case "false" => false
      case x => value
    }

    // Convert enums to their enum value
    val definition = this.dictionary.getDefinition(path)
    if (definition.value.isInstanceOf[EnumNode]) {
      val optionsPath = definition.value.asInstanceOf[EnumNode].enumOptionsPath
      typedValue = gov.irs.factgraph.types.Enum.apply(value, optionsPath)
    }

    // This should be possible with a switch statement
    if (definition.value.isInstanceOf[DollarNode]) {
      typedValue = Dollar(value)
    }

    this.set(path, typedValue)
  }

  def paths(): js.Array[String] = {
    this.dictionary.getPaths()
      .map(path => path.toString)
      .toJSArray
  }

  @JSExport("toJSON")
  def toJson(indent: Int = -1): String =
    this.persister.toJson(indent)

  def explainAndSolve(path: String): js.Array[js.Array[String]] =
    val rawExpl = this.explain(path)
    import js.JSConverters._
    return rawExpl.solves.map(l => l.map(p => p.toString).toJSArray).toJSArray

  @JSExport("save")
  def jsSave(): SaveReturnValue =
    val rawSave = this.save();
    import js.JSConverters._
    return SaveReturnValue(
      rawSave._1,
      rawSave._2.map(f => LimitViolationWrapper.fromLimitViolation(f)).toJSArray,
    )

  @JSExport("checkPersister")
  def jsCheckPersister(): js.Array[PersisterSyncIssueWrapper] =
    val raw = this.checkPersister();
    import js.JSConverters._
    return raw.map(f => PersisterSyncIssueWrapper.fromPersisterSyncIssue(f)).toJSArray

@JSExportTopLevel("GraphFactory")
object JSGraph:
  @JSExport("apply")
  def apply(
      dictionary: FactDictionary,
  ): JSGraph =
    this(dictionary, InMemoryPersisterJS.create())

  @JSExport("apply")
  def apply(
      dictionary: FactDictionary,
      persister: Persister,
  ): JSGraph =
    new JSGraph(dictionary, persister)

final class SaveReturnValue(
    val valid: Boolean,
    val limitViolations: js.Array[LimitViolationWrapper],
) extends js.Object

final class LimitViolationWrapper(
    var limitName: String,
    var factPath: String,
    val level: String,
    val limit: String,
    val actual: String,
) extends js.Object

object LimitViolationWrapper {
  def fromLimitViolation(lv: LimitViolation) =
    new LimitViolationWrapper(
      lv.limitName,
      lv.factPath,
      lv.LimitLevel.toString(),
      lv.limit,
      lv.actual,
    )
}

final class PersisterSyncIssueWrapper(
    val path: String,
    val message: String,
) extends js.Object

object PersisterSyncIssueWrapper {
  def fromPersisterSyncIssue(issue: PersisterSyncIssue) =
    new PersisterSyncIssueWrapper(
      issue.path,
      issue.message,
    )
}
