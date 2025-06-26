package gov.irs.factgraph
import upickle.default.ReadWriter
import java.util.UUID

enum PathItem derives ReadWriter:
  case Child(key: Symbol)
  case Parent

  // Collections
  case Member(id: UUID)
  case Wildcard
  case Unknown

  def isKnown: Boolean = this match
    case Child(_) | Member(_) => true
    case _                    => false

  def isAbstract: Boolean = this match
    case Child(_) | Wildcard => true
    case _                   => false

  def isWildcard: Boolean = this match
    case Wildcard => true
    case _        => false

  def isCollectionMember: Boolean = this match
    case Member(_) => true
    case _         => false

  def asAbstract: PathItem = this match
    case Member(_) | Unknown => Wildcard
    case _                   => this

  override def toString: String = this match
    case Wildcard   => PathItem.WildcardKey
    case Unknown    => PathItem.UnknownKey
    case Parent     => PathItem.ParentKey
    case Member(id) => s"${PathItem.MemberPrefix}${id}"
    case Child(key) => key.name

object PathItem:
  val WildcardKey = "*"
  private val UnknownKey = "?"
  private val ParentKey = ".."

  val MemberPrefix = '#'

  def apply(str: String): PathItem = str match
    case WildcardKey => PathItem.Wildcard
    case UnknownKey  => PathItem.Unknown
    case ParentKey   => PathItem.Parent
    case _ if str.charAt(0) == MemberPrefix =>
      PathItem.Member(UUID.fromString(str.substring(1)))
    case _ => PathItem.Child(Symbol(str))
