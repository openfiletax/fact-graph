package gov.irs.factgraph
import gov.irs.factgraph.persisters.*
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel, JSExportAll}
import scala.scalajs.js
import gov.irs.factgraph.limits.LimitViolation
import gov.irs.factgraph.definitions.fact.LimitLevel
import gov.irs.factgraph.monads.MaybeVector
import gov.irs.factgraph.monads.Result

@JSExportTopLevel("Graph")
@JSExportAll
class JSGraph(
    override val dictionary: FactDictionary,
    override val persister: Persister,
) extends Graph(dictionary, persister):

  def toStringDictionary(): js.Dictionary[String] =
    // This is a debug function to allow for quick inspection
    // of the graph
    import js.JSConverters._
    this.persister.toStringMap().toJSDictionary

  // Get a fact definition from the fact dictionary
  // In scala this is graph.apply
  def getFact(path: String) =
    import js.JSConverters._
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
