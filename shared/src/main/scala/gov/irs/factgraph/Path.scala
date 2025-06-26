package gov.irs.factgraph

import gov.irs.factgraph.monads.{MaybeVector, Result}
import gov.irs.factgraph.types.CollectionItem
import java.util.UUID

final case class Path(private val _items: List[PathItem], absolute: Boolean):
  def ++(rhs: Path): Path =
    if (rhs.absolute) rhs
    else Path(rhs._items ++ _items, absolute)

  def ++(rhs: Seq[PathItem]): Path =
    val items = rhs.foldLeft(_items)((items, item) => item +: items)
    Path(items, absolute)

  def :+(item: PathItem): Path = Path(item +: _items, absolute)

  def items: List[PathItem] = _items.reverse

  def head: Option[PathItem] = _items match
    case head :: _ => Some(head)
    case Nil       => None

  def parent: Option[Path] = _items match
    case head :: next => Some(Path(next, absolute))
    case Nil          => None

  def isAbstract: Boolean = absolute && _items.forall(_.isAbstract)

  def isKnown: Boolean = absolute && _items.forall(_.isKnown)

  def isWildcard: Boolean = absolute && _items.exists(_.isWildcard)

  def isCollectionMember: Boolean =
    absolute && _items.exists(_.isCollectionMember)

  def isCollectionItem: Boolean = absolute && _items(0).isCollectionMember

  def getMemberId: Option[UUID] =
    val itemOpt = _items.find((item) => item.isCollectionMember)
    if itemOpt.isEmpty then None
    else
      itemOpt.get match
        case PathItem.Member(uuid) => Some(uuid)
        case _                     => None

  def asAbstract: Path =
    items.foldLeft(items.head match
      case PathItem.Parent => Path("")
      case _               => Path("/"),
    )((pathSoFar: Path, nextPathItem: PathItem) =>
      pathSoFar :+ (
        nextPathItem match
          case PathItem.Member(_) => PathItem(PathItem.WildcardKey)
          case _                  => nextPathItem
      ),
    )

  def populateWildcards(
      graph: Graph,
      pathsWithoutWildcards: Seq[Path] = Nil,
  ): Seq[Path] =
    isWildcard match
      case false => this :: pathsWithoutWildcards.toList
      case true =>
        this
          .populateFirstWildcard(graph)
          .flatMap((pathPerhapsWithWildcards: Path) =>
            pathPerhapsWithWildcards
              .populateWildcards(graph, pathsWithoutWildcards),
          )

  private def populateFirstWildcard(graph: Graph): Seq[Path] =
    val pathItemsBeforeFirstWildcard = _items.reverse.takeWhile(!_.isWildcard)
    val pathItemsAfterFirstWildcard =
      _items.reverse.dropWhile(!_.isWildcard).dropWhile(_.isWildcard)

    val pathBeforeWildcard =
      pathItemsBeforeFirstWildcard.foldLeft(Path(Path.Delimiter)) { (pathSoFar, pathItem) =>
        pathSoFar :+ pathItem
      }
    try
      val vector =
        graph.getVect(pathBeforeWildcard :+ PathItem(PathItem.WildcardKey))

      vector.match
        case MaybeVector.Multiple[Result[CollectionItem]](multiple, complete) if complete =>
          for result <- multiple
          yield (pathBeforeWildcard :+ PathItem(
            s"${PathItem.MemberPrefix}${result.value.get.id}",
          )) ++ pathItemsAfterFirstWildcard
        case _ => Nil // collection is empty
    catch case e: UnsupportedOperationException => Nil

  override def toString: String =
    val prefix = if (absolute) Path.Delimiter else ""
    val path = items.map(_.toString()).mkString(Path.Delimiter)
    s"$prefix$path"

object Path:
  val Delimiter = "/"

  val Root: Path = new Path(Nil, true)
  val Relative: Path = new Path(Nil, false)

  def apply(str: String): Path = str match
    case Delimiter => Root
    case ""        => Relative
    case _ =>
      val item_strs = str.split(Delimiter, 0)
      val absolute = item_strs(0).isEmpty

      val items = item_strs
        .drop(if (absolute) 1 else 0)
        .map(PathItem(_))
        .reverse
        .toList

      Path(items, absolute)
