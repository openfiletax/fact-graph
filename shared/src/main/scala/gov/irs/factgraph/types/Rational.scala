package gov.irs.factgraph.types

import scala.util.matching.Regex
import gov.irs.factgraph.util.Math
import scala.scalajs.js.annotation.{JSExportTopLevel, JSExport, JSExportAll}
import upickle.default.ReadWriter

@JSExportTopLevel("Rational")
final case class Rational(private val n: Int, private val d: Int) derives ReadWriter:
  import Rational.RationalIsFractional._

  def numerator: Int = n
  def denominator: Int = d

  def reciprocal: Rational = n match
    case 0 => throw new ArithmeticException("reciprocal of zero")
    case _ => Rational(d, n)

  def simplify: Rational =
    val divisor = Math.gcd(n, d) * (if (d < 0) -1 else 1)

    Rational(n / divisor, d / divisor)

  def +(that: Rational): Rational = plus(this, that)
  def -(that: Rational): Rational = minus(this, that)
  def *(that: Rational): Rational = times(this, that)
  def /(that: Rational): Rational = div(this, that)

  def <(that: Rational): Boolean = lt(this, that)
  def >(that: Rational): Boolean = gt(this, that)
  def <=(that: Rational): Boolean = lteq(this, that)
  def >=(that: Rational): Boolean = gteq(this, that)

  override def toString: String = f"$n%d/$d%d"

given Conversion[Int, Rational] = Rational(_)

object Rational:
  private val Pattern: Regex = """^(-?\d+)/(-?\d+)$""".r

  @JSExportTopLevel("RationalFactory")
  def apply(n: Int, d: Int): Rational = d match
    case 0 => throw new IllegalArgumentException("denominator cannot be zero")
    case _ => new Rational(n, d)

  @JSExportTopLevel("RationalFactory")
  def apply(i: Int): Rational = new Rational(i, 1)

  @JSExportTopLevel("RationalFactory")
  def apply(str: String): Rational =
    Numeric[Rational].parseString(str) match
      case Some(x) => x
      case None =>
        throw new NumberFormatException(s"""For input string: "$str"""")

  implicit object RationalIsFractional extends Fractional[Rational]:
    def compare(x: Rational, y: Rational): Int =
      (x.n * y.d).compareTo(y.n * x.d)

    def fromInt(x: Int): Rational = Rational(x)

    def minus(x: Rational, y: Rational): Rational =
      if (x.d == y.d) Rational(x.n - y.n, x.d)
      else Rational(x.n * y.d - y.n * x.d, x.d * y.d).simplify

    def negate(x: Rational): Rational = Rational(-x.n, x.d)

    def plus(x: Rational, y: Rational): Rational =
      if (x.d == y.d) Rational(x.n + y.n, x.d)
      else Rational(x.n * y.d + y.n * x.d, x.d * y.d).simplify

    def times(x: Rational, y: Rational): Rational =
      Rational(x.n * y.n, x.d * y.d).simplify

    def div(x: Rational, y: Rational): Rational = y.n match
      case 0 => throw new ArithmeticException("divide by zero")
      case _ => Rational(x.n * y.d, x.d * y.n).simplify

    def parseString(str: String): Option[Rational] =
      Pattern.findFirstMatchIn(str) match
        case Some(m) => Some(Rational(m.group(1).toInt, m.group(2).toInt))
        case None    => None

    override def toDouble(x: Rational): Double =
      throw new UnsupportedOperationException(
        "toDouble might result in a loss of precision",
      )
    override def toFloat(x: Rational): Float =
      throw new UnsupportedOperationException(
        "toFloat might result in a loss of precision",
      )
    override def toInt(x: Rational): Int =
      throw new UnsupportedOperationException(
        "toInt might result in a loss of precision",
      )
    override def toLong(x: Rational): Long =
      throw new UnsupportedOperationException(
        "toLong might result in a loss of precision",
      )
