package gov.irs.factgraph

import gov.irs.factgraph
import gov.irs.factgraph.compnodes.{CollectionItemNode, CollectionNode, CompNode, WritableNode}
import gov.irs.factgraph.definitions.fact.{FactConfigTrait, LimitConfigTrait}
import gov.irs.factgraph.monads.*
import gov.irs.factgraph.limits.*
import gov.irs.factgraph.compnodes.Placeholder
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportAll

final class FactDefinition(
    private val cnBuilder: Factual ?=> CompNode,
    val path: Path,
    private val limitsBuilder: Factual ?=> Seq[Limit],
    val dictionary: FactDictionary,
) extends Factual:
  given Factual = this

  def attachToGraph(parent: Fact, key: PathItem): Fact =
    Fact(value, parent, limits, key, meta)

  def asTuple: (Path, FactDefinition) = (path, this)

  @JSExport
  lazy val value: CompNode = cnBuilder

  @JSExport
  lazy val limits: Seq[Limit] = limitsBuilder.map(x => x)

  @JSExport
  lazy val meta: Factual.Meta = Factual.Meta(
    size,
    abstractPath,
  )

  lazy val size: Factual.Size = value.getThunk match
    case MaybeVector.Single(_)      => Factual.Size.Single
    case MaybeVector.Multiple(_, _) => Factual.Size.Multiple

  @JSExport
  def abstractPath: Path = path

  override def get: MaybeVector[Result[value.Value]] = size match
    case Factual.Size.Single   => MaybeVector(Result.Incomplete)
    case Factual.Size.Multiple => MaybeVector(Nil, true)

  override def getThunk: MaybeVector[Thunk[Result[value.Value]]] = size match
    case Factual.Size.Single   => MaybeVector(Thunk(() => Result.Incomplete))
    case Factual.Size.Multiple => MaybeVector(Nil, true)

  override def explain: MaybeVector[Explanation] = size match
    case Factual.Size.Single   => MaybeVector(Explanation.NotAttachedToGraph)
    case Factual.Size.Multiple => MaybeVector(Nil, true)

  private def root: FactDefinition =
    dictionary(Path.Root).get

  private def parent: Option[FactDefinition] =
    path.parent.flatMap(_.head) match
      case Some(PathItem.Wildcard) =>
        for {
          parentPath <- path.parent
          grandparentPath <- parentPath.parent
          collection <- dictionary(grandparentPath)
          collectionItem <- collection(PathItem.Unknown)(0).value
        } yield collectionItem
      case _ =>
        for {
          parentPath <- path.parent
          fact <- dictionary(parentPath)
        } yield fact

  override def apply(path: Path): MaybeVector[Result[FactDefinition]] =
    if (path.absolute) root(path.items) else this(path.items)

  override def apply(key: PathItem): MaybeVector[Result[FactDefinition]] =
    this(List(key))

  private def apply(
      pathItems: List[PathItem],
  ): MaybeVector[Result[FactDefinition]] = pathItems match
    case PathItem.Parent :: next => getNext(parent, next)
    case PathItem.Child(_) :: _  => applyChild(pathItems)
    case PathItem.Wildcard :: _  => applyWildcard(pathItems)
    case PathItem.Member(_) :: _ => MaybeVector(Result.Incomplete)
    case PathItem.Unknown :: _   => applyUnknown(pathItems)
    case Nil                     => MaybeVector(Result.Complete(this))

  private def applyChild(
      pathItems: List[PathItem],
  ): MaybeVector[Result[FactDefinition]] = value match
    case CollectionItemNode(_, Some(alias)) =>
      this((alias :+ PathItem.Unknown) ++ pathItems)
    case _ => getNext(getChild(pathItems.head), pathItems.tail)

  private def applyWildcard(
      pathItems: List[PathItem],
  ): MaybeVector[Result[FactDefinition]] = value match
    case CollectionNode(_, Some(alias)) =>
      this(alias ++ pathItems)
    case CollectionNode(_, None) =>
      getNext(getExtract(PathItem.Unknown), pathItems.tail).toMultiple
    case _ => MaybeVector(Result.Incomplete)

  private def applyUnknown(
      pathItems: List[PathItem],
  ): MaybeVector[Result[FactDefinition]] = value match
    case CollectionNode(_, Some(alias)) =>
      this(alias ++ pathItems)
    case CollectionNode(_, None) =>
      getNext(getExtract(pathItems.head), pathItems.tail)
    case _ => MaybeVector(Result.Incomplete)

  private def getNext(
      optFact: Option[FactDefinition],
      next: List[PathItem],
  ): MaybeVector[Result[FactDefinition]] =
    for {
      result <- MaybeVector(Result(optFact))
      vect <- result.value match
        case Some(fact) => fact(next)
        case None       => MaybeVector(Result.Incomplete)
    } yield vect

  private def getChild(key: PathItem): Option[FactDefinition] =
    getExtract(key).orElse(dictionary(path :+ key))

  private def getExtract(key: PathItem): Option[FactDefinition] =
    value
      .extract(key)
      .map(node =>
        new FactDefinition(
          Factual ?=> node,
          path :+ key.asAbstract,
          Seq.empty,
          dictionary,
        ),
      )

object FactDefinition:
  def apply(
      cnBuilder: Factual ?=> CompNode,
      path: Path,
      limits: Factual ?=> Seq[Limit],
      dictionary: FactDictionary,
  ): FactDefinition =
    require(path.isAbstract)

    val definition = new FactDefinition(cnBuilder, path, limits, dictionary)
    dictionary.addDefinition(definition)

    definition

  def fromConfig(e: FactConfigTrait)(using FactDictionary): FactDefinition =
    // if neither of them or both of them
    if (e.writable.isEmpty && e.derived.isEmpty || (e.writable.isDefined && e.derived.isDefined))
      throw new IllegalArgumentException(
        "Fact must have exactly one Writable or Derived",
      )
    val isWritable = e.writable.isDefined

    val cnBuilder: Factual ?=> CompNode =
      val node =
        if (isWritable) WritableNode.fromConfig(e.writable.get)
        else CompNode.fromDerivedConfig(e.derived.get)

      e.placeholder match
        case Some(default) =>
          Placeholder(node, CompNode.fromDerivedConfig(default))
        case None => node

    val limits: Factual ?=> Seq[Limit] =
      if (isWritable)
        e.writable.get.limits
          .map(x => Limit.fromConfig(x))
          .toSeq
          .concat(WritableNode.fromConfig(e.writable.get).getIntrinsicLimits())
      else
        Seq.empty

    val dictionary = summon[FactDictionary]
    this(cnBuilder, Path(e.path), limits, dictionary)
