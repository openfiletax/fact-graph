package gov.irs.factgraph.monads
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

enum JSEither[+A, +B] {
  case Left(v: A)
  case Right(v: B)

  @JSExport
  def left: A | Null = this match
    case Left(v) => v
    case _       => null

  @JSExport
  def right: B | Null = this match
    case Right(v) => v
    case _        => null

  @JSExport
  def isRight: Boolean = this match
    case Right(_) => true
    case _        => false

  @JSExport
  def isLeft: Boolean = !this.isRight

  @JSExport
  def map[C](f: js.Function1[B, js.Object]): js.Object | Null = this match
    case Right(v) => f(v)
    case _        => null

  @JSExport
  def mapLeftRight[C](
      lf: js.Function1[A, js.Object],
      rf: js.Function1[B, js.Object],
  ): js.Object = this match
    case Left(e)  => lf(e)
    case Right(v) => rf(v)

  // TODO: All of the other useful methods for monads are TK

}
