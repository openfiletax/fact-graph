package gov.irs.factgraph.types
import scala.scalajs.js.annotation.{JSExportTopLevel, JSExport, JSExportAll}
import upickle.default.ReadWriter

import scala.beans.BeanProperty
import scala.util.matching.Regex
import java.lang.Enum

import gov.irs.factgraph.validation.{ValidationFailure, ValidationFailureReason}

sealed trait E164Number derives ReadWriter:
  val countryCode: String
  val subscriberNumber: String
  override def toString(): String =
    s"+${this.countryCode}${this.subscriberNumber}"

@JSExportAll
enum UserFriendlyPhoneNumberFailureReason:
  case IncorrectPhoneLength
  case InvalidAreaCode
  case InvalidOfficeCode
  case InvalidCountryCode
  case InvalidPhoneNumberGeneric
  case NonNumericPhoneNumber

@JSExportAll
enum E164NumberFailureReason extends Enum[E164NumberFailureReason], ValidationFailureReason:
  type UserFriendlyReason = UserFriendlyPhoneNumberFailureReason
  case InvalidAreaCodeStartDigit0
  case InvalidAreaCodeStartDigit1
  case InvalidAreaCodeMiddleDigit9
  case InvalidAreaCodeLength
  case InvalidOfficeCodeStartDigit0
  case InvalidOfficeCodeStartDigit1
  case InvalidOfficeCodeLength
  case InvalidLineNumberLength
  case NonDigitInput
  case InvalidE164NumberLength
  case InvalidCountryCode
  case MalformedPhoneNumber

  override def toUserFriendlyReason(): UserFriendlyPhoneNumberFailureReason =
    this match
      // NOTE: The frontend will currently only see `InvalidAreaCode`, `InvalidOfficeCode` and `InvalidPhoneNumberGeneric` but this is being implemented for the full set of failure reasons for completeness and future-proofing
      case (
            InvalidAreaCodeStartDigit0 | InvalidAreaCodeStartDigit1 | InvalidAreaCodeLength |
            InvalidAreaCodeMiddleDigit9
          ) =>
        UserFriendlyPhoneNumberFailureReason.InvalidAreaCode
      case InvalidCountryCode =>
        UserFriendlyPhoneNumberFailureReason.InvalidCountryCode
      case InvalidE164NumberLength =>
        UserFriendlyPhoneNumberFailureReason.IncorrectPhoneLength
      case (InvalidOfficeCodeLength | InvalidOfficeCodeStartDigit0 | InvalidOfficeCodeStartDigit1) =>
        UserFriendlyPhoneNumberFailureReason.InvalidOfficeCode
      case (InvalidLineNumberLength | MalformedPhoneNumber) =>
        UserFriendlyPhoneNumberFailureReason.InvalidPhoneNumberGeneric
      case NonDigitInput =>
        UserFriendlyPhoneNumberFailureReason.NonNumericPhoneNumber

@JSExportAll
final case class E164NumberValidationFailure(
    private val message: String = "",
    private val cause: Throwable = None.orNull,
    val validationMessage: E164NumberFailureReason,
) extends IllegalArgumentException(message, cause),
      ValidationFailure[E164NumberFailureReason]

private def phoneNumberPreCondition(
    test: Boolean,
    message: String,
    validationMessage: E164NumberFailureReason,
): Unit =
  if (!test)
    throw E164NumberValidationFailure(message, None.orNull, validationMessage)

@JSExportTopLevel("UsPhoneNumber")
@JSExportAll
final case class UsPhoneNumber(
    @BeanProperty areaCode: String,
    @BeanProperty officeCode: String,
    @BeanProperty lineNumber: String,
) extends E164Number:
  // check lengths
  precondition(
    areaCode.length == 3,
    "Area Code of wrong length",
    E164NumberFailureReason.InvalidAreaCodeLength,
  )
  precondition(
    officeCode.length == 3,
    "Office Code of wrong length",
    E164NumberFailureReason.InvalidOfficeCodeLength,
  )
  precondition(
    lineNumber.length == 4,
    "Line-number of wrong length",
    E164NumberFailureReason.InvalidLineNumberLength,
  )

  // check that all are digits
  precondition(
    List(areaCode, officeCode, lineNumber).forall(arg => arg.forall(_.isDigit)),
    "Phone Number contains non-digits",
    E164NumberFailureReason.NonDigitInput,
  )

  // Per Wikipedia:
  // Using 0 or 1 as the first digit of an area code or central office code is invalid, as is a 9 as the middle digit
  // of an area code; these are trunk prefixes or reserved for North American Numbering Plan expansion.
  precondition(
    areaCode(0) != '0',
    "Area-code cannot start with 0",
    E164NumberFailureReason.InvalidAreaCodeStartDigit0,
  )
  precondition(
    areaCode(0) != '1',
    "Area-code cannot start with 1",
    E164NumberFailureReason.InvalidAreaCodeStartDigit1,
  )
  precondition(
    officeCode(0) != '0',
    "Office-code cannot start with 0",
    E164NumberFailureReason.InvalidOfficeCodeStartDigit0,
  )
  precondition(
    officeCode(0) != '1',
    "Office-code cannot start with 1",
    E164NumberFailureReason.InvalidOfficeCodeStartDigit1,
  )
  precondition(
    areaCode(1) != '9',
    "Area Code cannot contain 9 as the middle digit",
    E164NumberFailureReason.InvalidAreaCodeMiddleDigit9,
  )

  val countryCode = "1";
  val subscriberNumber: String =
    s"${this.areaCode}${this.officeCode}${this.lineNumber}"

  private[this] def precondition = phoneNumberPreCondition

  def getFormatted(): String =
    s"${this.areaCode}-${this.officeCode}-${this.lineNumber}"

@JSExportTopLevel("InternationalPhoneNumber")
final case class InternationalPhoneNumber(
    @BeanProperty countryCode: String,
    @BeanProperty subscriberNumber: String,
) extends E164Number:
  precondition(
    countryCode.length >= 1,
    "Country code must be at least one digit long",
    E164NumberFailureReason.InvalidCountryCode,
  )
  precondition(
    countryCode.length <= 3,
    "Country code cannot be more than 3 digits",
    E164NumberFailureReason.InvalidCountryCode,
  )
  precondition(
    countryCode.length + subscriberNumber.length <= 15,
    "E164 Number must be 15 or fewer digits",
    E164NumberFailureReason.InvalidE164NumberLength,
  )
  precondition(
    List(countryCode, subscriberNumber).forall(arg => arg.forall(_.isDigit)),
    "Phone Number contains non-digits",
    E164NumberFailureReason.NonDigitInput,
  )

  private[this] def precondition = phoneNumberPreCondition

  def getFormatted(): String = this.toString()

object PhoneNumber:
  private val UsPhonePattern: Regex = """^\+1(\d{3})(\d{3})(\d{4})$""".r
  private val E164Pattern: Regex = """^\+([1-9]{1,3})(\d{1,14})$""".r

  @JSExportTopLevel("PhoneNumberFactory")
  def apply(s: String): E164Number = s match
    case UsPhonePattern(area, office, line) =>
      new UsPhoneNumber(area, office, line)
    case E164Pattern(country, subscriber) =>
      new InternationalPhoneNumber(country, subscriber)
    case _ => throw IllegalArgumentException("Invalid E164-formatted number")
