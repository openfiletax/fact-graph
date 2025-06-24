package gov.irs.factgraph

import gov.irs.factgraph.compnodes.{CollectionItemNode, CollectionNode, CompNode, RootNode}
import gov.irs.factgraph.monads.*
import gov.irs.factgraph.types.{Collection, CollectionItem, WritableType}
import gov.irs.factgraph.limits.*

import java.util.UUID
import scala.annotation.tailrec
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportAll

final class Fact(
    @JSExport val value: CompNode,
    @JSExport val path: Path,
    @JSExport val limits: Seq[Limit],
    @JSExport val graph: Graph,
    @JSExport val parent: Option[Fact],
    @JSExport val meta: Factual.Meta,
) extends Factual:
  given Factual = this
  export meta.*

  @tailrec
  def root: Fact = parent match
    case Some(parent) => parent.root
    case None         => this

  override def get: MaybeVector[Result[value.Value]] =
    graph.resultCache
      .getOrElseUpdate(path, { value.get })
      .asInstanceOf[MaybeVector[Result[value.Value]]]

  override def getThunk: MaybeVector[Thunk[Result[value.Value]]] =
    value.getThunk

  override def explain: MaybeVector[Explanation] = value.explain

  /** Set the value of a writable fact.
    *
    * @param a
    *   The new value.
    * @param allowCollectionItemDelete
    *   Whether to allow setting a Collection in a way that removes items.
    */
  def set(a: WritableType, allowCollectionItemDelete: Boolean = false): Unit =
    if (!value.expr.isWritable)
      throw new Exception(s"${path} is not writable")

    if (!allowCollectionItemDelete)
      a match
        case Collection(newValues) =>
          graph.getVect(path) match
            case MaybeVector.Single(Result(Collection(oldValues), _)) =>
              if (oldValues.exists(oldValue => !newValues.contains(oldValue)))
                throw new Exception(
                  s"Cannot use set() to remove items(s) from collection ${path}",
                )
            case _ =>
        case _ =>

    if (!value.ValueClass.isAssignableFrom(a.getClass()))
      throw new Exception(
        s"${path} is expecting '${value.ValueClass}', received '${a.getClass()}' instead",
      )

    graph.persister.setFact(this, a)

  def validate(): Seq[LimitViolation] =
    limits.map(x => x.run()).filter(x => x.isDefined).map(x => x.get)

  def delete(): Unit =
    val collectionPath = parent.get.path
    graph.getVect(collectionPath) match
      case MaybeVector.Single(Result(Collection(collection), _)) =>
        val deleted = path.getMemberId.get
        graph.persister.deleteFact(this, true)
        graph(collectionPath)(0).get
          .set(Collection(collection.filter((uuid) => uuid != deleted)), true)
      case _ if value.expr.isWritable =>
        graph.persister.deleteFact(this, false)
      case _ =>

  override def apply(path: Path): MaybeVector[Result[Fact]] =
    this(path, true)

  override def apply(key: PathItem): MaybeVector[Result[Fact]] =
    this(List(key), true)

  private def apply(
      path: Path,
      accComplete: Boolean,
  ): MaybeVector[Result[Fact]] =
    if (path.absolute) root(path.items, accComplete)
    else this(path.items, accComplete)

  private def apply(
      pathItems: List[PathItem],
      accComplete: Boolean,
  ): MaybeVector[Result[Fact]] = pathItems match
    case PathItem.Parent :: next  => applyNext(parent, next, accComplete)
    case PathItem.Child(_) :: _   => applyChild(pathItems, accComplete)
    case PathItem.Wildcard :: _   => applyWildcard(pathItems, accComplete)
    case PathItem.Member(id) :: _ => applyMember(id, pathItems, accComplete)
    case PathItem.Unknown :: _    => applyUnknown(pathItems)
    case Nil                      => MaybeVector(Result(this, accComplete))

  private def applyChild(
      pathItems: List[PathItem],
      accComplete: Boolean,
  ): MaybeVector[Result[Fact]] = value match
    case CollectionItemNode(item, Some(alias)) =>
      applyNextFollowingAlias(item, alias, pathItems, accComplete)
    case _ => applyNext(getChild(pathItems.head), pathItems.tail, accComplete)

  private def applyWildcard(
      pathItems: List[PathItem],
      accComplete: Boolean,
  ): MaybeVector[Result[Fact]] = value match
    case CollectionNode(collection, Some(alias)) =>
      mapCollectionItems(collection, accComplete) { id =>
        followAlias(alias, PathItem.Member(id) +: pathItems.tail, true)
      }
    case CollectionNode(collection, None) =>
      mapCollectionItems(collection, accComplete) { id =>
        applyNext(getMember(PathItem.Member(id)), pathItems.tail, true)
      }
    case _ => MaybeVector(Result.Incomplete)

  private def applyMember(
      id: UUID,
      pathItems: List[PathItem],
      accComplete: Boolean,
  ): MaybeVector[Result[Fact]] = value match
    case CollectionNode(collection, Some(alias)) =>
      mapCollectionItemIfInCollection(collection, id) {
        followAlias(alias, pathItems, accComplete)
      }
    case CollectionNode(collection, None) =>
      mapCollectionItemIfInCollection(collection, id) {
        applyNext(getMember(pathItems.head), pathItems.tail, accComplete)
      }
    case _ => MaybeVector(Result.Incomplete)

  private def applyUnknown(
      pathItems: List[PathItem],
  ): MaybeVector[Result[Fact]] = value match
    case CollectionNode(collection, Some(alias)) =>
      followAlias(alias, pathItems, false)
    case CollectionNode(collection, None) =>
      applyNext(getMember(pathItems.head), pathItems.tail, false)
    case _ => MaybeVector(Result.Incomplete)

  private def applyNext(
      optFact: Option[Fact],
      next: List[PathItem],
      accComplete: Boolean,
  ): MaybeVector[Result[Fact]] =
    for {
      result <- MaybeVector(Result(optFact))
      vect <- result match
        case Result(fact, complete) => fact(next, complete && accComplete)
        case _                      => MaybeVector(Result.Incomplete)
    } yield vect

  private def applyNextFollowingAlias(
      collectionItemExpr: Expression[CollectionItem],
      alias: Path,
      next: List[PathItem],
      accComplete: Boolean,
  ): MaybeVector[Result[Fact]] =
    for {
      result <- collectionItemExpr.get
      vect <- result match
        case Result(item, complete) =>
          followAlias(
            alias,
            PathItem.Member(item.id) +: next,
            complete && accComplete,
          )
        case _ => followAlias(alias, PathItem.Unknown +: next, false)
    } yield vect

  private def followAlias(
      alias: Path,
      next: List[PathItem],
      accComplete: Boolean,
  ): MaybeVector[Result[Fact]] =
    this(alias ++ next, accComplete)

  private def getChild(key: PathItem): Option[Fact] =
    val childPath = path :+ key
    graph.factCache.getOrElseUpdate(childPath, { makeFact(key) })

  private def getMember(key: PathItem): Option[Fact] =
    val memberPath = path :+ key
    graph.factCache.getOrElseUpdate(memberPath, { makeExtract(key) })

  private def makeFact(key: PathItem): Option[Fact] =
    makeExtract(key).orElse({
      graph
        .dictionary(meta.abstractPath :+ key)
        .map(_.attachToGraph(this, key))
    })

  private def makeExtract(key: PathItem): Option[Fact] =
    value
      .extract(key)
      .map(node =>
        val extractMeta = Factual.Meta(
          size,
          meta.abstractPath :+ key.asAbstract,
        )
        Fact(node, this, limits, key, extractMeta),
      )

  private def mapCollectionItems(
      collectionExpr: Expression[Collection],
      accComplete: Boolean,
  ): (f: UUID => MaybeVector[Result[Fact]]) => MaybeVector[Result[Fact]] =
    f =>
      for {
        collection <- collectionExpr.get
        vect <- collection match
          case Result(collection, complete) =>
            for {
              id <- MaybeVector(collection.items, complete && accComplete)
              result <- f(id)
            } yield result
          case _ => MaybeVector(Nil, false)
      } yield vect

  private def mapCollectionItemIfInCollection(
      collectionExpr: Expression[Collection],
      id: UUID,
  ): (=> MaybeVector[Result[Fact]]) => MaybeVector[Result[Fact]] =
    x =>
      for {
        collection <- collectionExpr.get
        result <- collection match
          case Result(collection, _) if collection.items.contains(id) => x
          case _                                                      => MaybeVector(Result.Incomplete)
      } yield result

object Fact:
  private val RootNode = new RootNode()
  private val RootMeta = Factual.Meta(
    Factual.Size.Single,
    Path.Root,
  )

  def apply(graph: Graph): Fact = new Fact(
    RootNode,
    Path.Root,
    Seq.empty,
    graph,
    None,
    RootMeta,
  )

  def apply(
      value: CompNode,
      parent: Fact,
      limits: Seq[Limit],
      pathItem: PathItem,
      meta: Factual.Meta,
  ): Fact =
    new Fact(
      value,
      parent.path :+ pathItem,
      limits,
      parent.graph,
      Some(parent),
      meta,
    )
