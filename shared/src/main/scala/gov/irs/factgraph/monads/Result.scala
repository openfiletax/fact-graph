package gov.irs.factgraph.monads
import scala.scalajs.js.annotation.JSExport

enum Result[+A]:
  case Complete(v: A)
  case Placeholder(v: A)
  case Incomplete

  @JSExport
  def complete: Boolean = this match
    case Complete(_) => true
    case _           => false

  @JSExport
  def hasValue: Boolean = this match
    case Incomplete => false
    case _          => true

  @JSExport
  def value: Option[A] = this match
    case Complete(x)    => Some(x)
    case Placeholder(x) => Some(x)
    case Incomplete     => None

  @JSExport
  def get: A = this match
    case Complete(x)    => x
    case Placeholder(x) => x
    case Incomplete =>
      throw new NoSuchElementException(
        "attempted to retrieve the value of an incomplete result",
      )

  @JSExport
  def getOrElse[B >: A](default: => B): B = this match
    case Complete(x)    => x
    case Placeholder(x) => x
    case Incomplete     => default

  @JSExport
  def orElse[B >: A](default: => B): Result[B] = this match
    case Incomplete => Result.Placeholder(default)
    case _          => this

  @JSExport
  def asPlaceholder: Result[A] = this match
    case Complete(x) => Placeholder(x)
    case _           => this

  @JSExport
  def map[B](f: A => B): Result[B] = this match
    case Complete(x)    => Complete(f(x))
    case Placeholder(x) => Placeholder(f(x))
    case Incomplete     => Incomplete

  @JSExport
  def flatMap[B](f: A => Result[B]): Result[B] = this match
    case Complete(x)    => f(x)
    case Placeholder(x) => f(x).asPlaceholder
    case Incomplete     => Incomplete

  @JSExport
  def foreach(f: A => Unit): Unit = this match
    case Complete(x)    => f(x)
    case Placeholder(x) => f(x)
    case Incomplete     =>

  @JSExport
  override def toString: String =
    val valueFmt = value.getOrElse("???")
    val completeFmt = if (complete) "complete" else "incomplete"
    s"Result($valueFmt, $completeFmt)"

  @JSExport
  def typeName: String = this match
    case Complete(v)    => v.getClass().toString()
    case Placeholder(v) => v.getClass().toString()
    case Incomplete     => ""

object Result:
  def apply[A](a: A, complete: Boolean): Result[A] =
    if (complete) Result.Complete(a) else Result.Placeholder(a)

  def apply[A](a: Option[A]): Result[A] = a match
    case Some(a) => Result.Complete(a)
    case None    => Result.Incomplete

  def unapply[A](result: Result[A]): Option[(A, Boolean)] = result match
    case Complete(x)    => Some(x, true)
    case Placeholder(x) => Some(x, false)
    case Incomplete     => None
