package gov.irs.factgraph.types
import java.lang.Enum
import scala.scalajs.js.annotation.{JSExportTopLevel, JSExport, JSExportAll}
import upickle.default.{ReadWriter => RW, readwriter}
import gov.irs.factgraph.validation.{ValidationFailureReason, ValidationFailure}

@JSExportTopLevel("Dollar")
opaque type Dollar = BigDecimal

@JSExportAll
enum DollarFailureReason extends Enum[DollarFailureReason], ValidationFailureReason:
  type UserFriendlyReason = DollarFailureReason

  case InvalidDollar

  case TooManyDecimals
  case InvalidHyphens
  case TooManyFractionalDigits
  case CombinedHyphensAndParentheses
  case InvalidParentheses
  case InvalidCharacters
  case ExceedsMaxLimit
  case ExceedsMinLimit

  def toUserFriendlyReason() =
    this // Error reason is already user friendly

@JSExportAll
final case class DollarValidationFailure(
    val message: String = "",
    private val cause: Throwable = None.orNull,
    val validationMessage: DollarFailureReason,
) extends IllegalArgumentException(message, cause),
      ValidationFailure[DollarFailureReason];

extension (x: Dollar)
  def +(y: Dollar): Dollar = Dollar.DollarIsFractional.plus(x, y)
  def -(y: Dollar): Dollar = Dollar.DollarIsFractional.minus(x, y)
  def *(y: Dollar): Dollar = Dollar.DollarIsFractional.times(x, y)
  def /(y: Dollar): Dollar = Dollar.DollarIsFractional.div(x, y)

  def <(y: Dollar): Boolean = Dollar.DollarIsFractional.lt(x, y)
  def >(y: Dollar): Boolean = Dollar.DollarIsFractional.gt(x, y)
  def <=(y: Dollar): Boolean = Dollar.DollarIsFractional.lteq(x, y)
  def >=(y: Dollar): Boolean = Dollar.DollarIsFractional.gteq(x, y)

  def intValue: Int = x.intValue

  def round: Dollar = Dollar(
    x.setScale(0, scala.math.BigDecimal.RoundingMode.HALF_UP),
  )

given Conversion[Int, Dollar] = Dollar.DollarIsFractional.fromInt(_)
given Conversion[Rational, Dollar] with
  def apply(rat: Rational): Dollar = Dollar(
    BigDecimal(rat.numerator.toFloat / rat.denominator),
  )

object Dollar:
  private val Scale = 2
  private val RoundingMode = scala.math.BigDecimal.RoundingMode.HALF_EVEN

  /** String representations meet all the following requirements:
    *   - At least one non-fractional digit
    *   - Either a) ends with exactly one '.' followed up by one or two digits b) has no '.'
    *   - Either a) preceded by exactly zero or one '-' b) entirely wrapped by exactly zero or one pair of parentheses
    */
  private val ParentheticalNumberPattern = """[\(\)](.+?)[\(\)]""".r
  private val ValidCharsetPattern = """-?([0-9,]+)\.?([0-9]+)?""".r
  implicit val dollarReadWriter: RW[Dollar] =
    readwriter[String].bimap[Dollar](_.toString, Dollar(_))

  def apply(d: BigDecimal): Dollar = d.setScale(Scale, RoundingMode)
  def apply(s: String, allowRounding: Boolean = true): Dollar =
    val decimalCount = s.count(_ == '.')
    precondition(
      decimalCount == 0 || decimalCount == 1,
      "At most one decimal value is allowed.",
      DollarFailureReason.TooManyDecimals,
    )

    val hyphenCount = s.count(_ == '-')

    // Convert parenthetical negatives to hypens and strip any commas
    val normalizedString = (s match
      case ParentheticalNumberPattern(absoluteValue) =>
        precondition(
          hyphenCount == 0,
          "Cannot combine parentheses and hyphens",
          DollarFailureReason.CombinedHyphensAndParentheses,
        )

        s"-$absoluteValue"
      case _ => s
    )

    // Any parentheses not caught by the previous regex are invalid
    val parenthesesCount =
      normalizedString.count(_ == '(') + normalizedString.count(_ == ')')
    precondition(
      parenthesesCount == 0,
      s"Parentheses not allowed unless they are both the first and last character",
      DollarFailureReason.InvalidParentheses,
    )

    val isHyphenated = normalizedString.startsWith("-")
    val isJustHyphen = normalizedString == "-"
    precondition(
      !isJustHyphen && (hyphenCount == 0 || (isHyphenated && hyphenCount == 1)),
      "At most one hyphen allowed and must be at the beginner of the number if provided",
      DollarFailureReason.InvalidHyphens,
    )

    normalizedString match
      case ValidCharsetPattern(_, fractionalDigits) =>
        // Regex provides null if an optional group is not present
        val isNull = null == fractionalDigits
        precondition(
          allowRounding || isNull || fractionalDigits.length <= 2,
          "Invalid Fractional Digits",
          DollarFailureReason.TooManyFractionalDigits,
        )
      case _ =>
        precondition(
          false,
          s"Invalid Characters $normalizedString",
          DollarFailureReason.InvalidCharacters,
        )

    // Strip commas before delegating to BigDecimal
    this(BigDecimal(normalizedString.replace(",", "")))
  private[this] def precondition(
      test: Boolean,
      message: String,
      validationMessage: DollarFailureReason,
  ): Unit =
    if (!test)
      throw DollarValidationFailure(message, None.orNull, validationMessage)
  implicit object DollarIsFractional extends Fractional[Dollar]:
    override def compare(x: Dollar, y: Dollar): Int = x.compareTo(y)
    override def fromInt(x: Int): Dollar = Dollar(BigDecimal(x))
    override def minus(x: Dollar, y: Dollar): Dollar = x - y
    override def negate(x: Dollar): Dollar = -x
    override def plus(x: Dollar, y: Dollar): Dollar = x + y
    override def times(x: Dollar, y: Dollar): Dollar = Dollar(x * y)
    override def div(x: Dollar, y: Dollar): Dollar = Dollar(x / y)

    override def parseString(str: String): Option[Dollar] =
      try {
        Some(Dollar(str))
      } catch {
        case _: DollarValidationFailure => None
        case _: NumberFormatException   => None
      }

    override def toDouble(x: Dollar): Double =
      throw new UnsupportedOperationException(
        "toDouble might result in a loss of precision",
      )
    override def toFloat(x: Dollar): Float =
      throw new UnsupportedOperationException(
        "toFloat might result in a loss of precision",
      )
    override def toInt(x: Dollar): Int =
      throw new UnsupportedOperationException(
        "toInt might result in a loss of precision",
      )
    override def toLong(x: Dollar): Long =
      throw new UnsupportedOperationException(
        "toLong might result in a loss of precision",
      )
