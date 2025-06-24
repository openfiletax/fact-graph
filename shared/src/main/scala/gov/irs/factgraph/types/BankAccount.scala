package gov.irs.factgraph.types
import upickle.default.ReadWriter

import java.lang.Enum
import scala.beans.BeanProperty
import scala.scalajs.js.annotation.{JSExportTopLevel, JSExport, JSExportAll}
import scala.scalajs.js

import gov.irs.factgraph.validation.{ValidationFailure, ValidationFailureReason}
import scala.util.matching.Regex

@JSExportAll
enum UserFriendlyBankAccountFailureReason:
  case InvalidBankAccount
  case InvalidAccountType
  case InvalidRoutingNumber
  case MalformedRoutingNumber
  case MalformedAccountNumber
  case InvalidAllZerosAccountNumber

@JSExportAll
enum BankAccountFailureReason extends Enum[BankAccountFailureReason], ValidationFailureReason:
  type UserFriendlyReason = UserFriendlyBankAccountFailureReason

  case InvalidBankAccount

  // AccountType errors
  case InvalidAccountType

  // RoutingNumber errors
  case MalformedRoutingNumber
  case InvalidRoutingNumberChecksum
  case InvalidRoutingNumber

  // AccountNumber errors
  case MalformedAccountNumber
  case InvalidAllZerosAccountNumber

  def toUserFriendlyReason() = this match
    case InvalidBankAccount =>
      UserFriendlyBankAccountFailureReason.InvalidBankAccount
    case InvalidAccountType =>
      UserFriendlyBankAccountFailureReason.InvalidAccountType
    case MalformedRoutingNumber | InvalidRoutingNumberChecksum =>
      UserFriendlyBankAccountFailureReason.MalformedRoutingNumber
    case InvalidRoutingNumber =>
      UserFriendlyBankAccountFailureReason.InvalidRoutingNumber
    case MalformedAccountNumber =>
      UserFriendlyBankAccountFailureReason.MalformedAccountNumber
    case InvalidAllZerosAccountNumber =>
      UserFriendlyBankAccountFailureReason.InvalidAllZerosAccountNumber

@JSExportAll
final case class BankAccountFieldValidationFailure(
    val message: String = "",
    private val cause: Throwable = None.orNull,
    val validationMessage: BankAccountFailureReason,
) extends IllegalArgumentException(message, cause),
      ValidationFailure[BankAccountFailureReason];

type BankAccountFieldName = "accountType" | "routingNumber" | "accountNumber"

@JSExportAll
final case class BankAccountValidationFailure(
    val message: String = "",
    private val cause: Throwable = None.orNull,
    val validationMessage: BankAccountFailureReason,
    val fieldErrors: collection.Map[
      BankAccountFieldName,
      BankAccountFieldValidationFailure,
    ] = collection.Map(),
) extends IllegalArgumentException(message, cause),
      ValidationFailure[BankAccountFailureReason];

@JSExportAll
enum BankAccountType extends Enum[BankAccountType]:
  case Checking
  case Savings

@JSExportTopLevel("BankAccount")
@JSExportAll
final case class BankAccount(
    /** The account type. Must be one of Checking or Savings
      */
    @BeanProperty accountType: String,

    /** A 9 character, numeric id, representing the routing number of the account
      *
      * Represented as a string to allow leading zeroes
      */
    @BeanProperty routingNumber: String,
    /** An alphanumeric id with 5-17 characters, representing the account number of the account
      */
    @BeanProperty accountNumber: String,
) derives ReadWriter:
  final val AllowedAccountTypes = BankAccountType.values.map(v => v.name())

  private[this] var fieldErrors = collection.mutable
    .Map[BankAccountFieldName, BankAccountFieldValidationFailure]()

  // Validate accountType
  try {
    precondition(
      AllowedAccountTypes.contains(accountType),
      "accountType must be a valid BankAccountType",
      BankAccountFailureReason.InvalidAccountType,
    )
  } catch {
    case e: BankAccountFieldValidationFailure => fieldErrors("accountType") = e
    case _ =>
      fieldErrors("accountType") = BankAccountFieldValidationFailure(
        "accountType preconditions failed with unknown failure",
        None.orNull,
        BankAccountFailureReason.InvalidAccountType,
      )
  }

  // Validate routingNumber
  try {
    val RoutingNumberPattern: Regex = """[0-9]{9}""".r
    precondition(
      RoutingNumberPattern.matches(routingNumber),
      "routingNumber must be a 9 character string containing only numbers ",
      BankAccountFailureReason.InvalidRoutingNumber,
    )

    val RoutingNumberPrefixPattern: Regex =
      """(01|02|03|04|05|06|07|08|09|10|11|12|21|22|23|24|25|26|27|28|29|30|31|32)[0-9]{7}""".r
    precondition(
      RoutingNumberPrefixPattern.matches(routingNumber),
      "routingNumber must be a 9 character string containing only numbers and only have certain prefixes",
      BankAccountFailureReason.MalformedRoutingNumber,
    )

    val checksum = routingNumberChecksum(routingNumber)
    precondition(
      checksum % 10 == 0,
      s"routingNumber checksum is invalid ($checksum)",
      BankAccountFailureReason.InvalidRoutingNumberChecksum,
    )
  } catch {
    case e: BankAccountFieldValidationFailure =>
      fieldErrors("routingNumber") = e
    case _ =>
      fieldErrors("routingNumber") = BankAccountFieldValidationFailure(
        "routingNumber preconditions failed with unknown failure",
        None.orNull,
        BankAccountFailureReason.InvalidBankAccount,
      )
  }

  // Validate accountNumber
  try {
    val AllZerosPattern: Regex = """^0{5,17}$""".r

    precondition(
      !AllZerosPattern.matches(accountNumber),
      "accountNumber must not contain only zeros",
      BankAccountFailureReason.InvalidAllZerosAccountNumber,
    )
  } catch {
    case e: BankAccountFieldValidationFailure =>
      fieldErrors("accountNumber") = e
    case _ =>
      fieldErrors("accountNumber") = BankAccountFieldValidationFailure(
        "accountNumber preconditions failed with unknown failure",
        None.orNull,
        BankAccountFailureReason.InvalidBankAccount,
      )
  }
  try {
    val AccountNumberPattern: Regex = """[0-9A-Z]{5,17}""".r

    precondition(
      AccountNumberPattern.matches(accountNumber),
      "accountNumber must an alphanumeric string with between 5 and 17 characters",
      BankAccountFailureReason.MalformedAccountNumber,
    )
  } catch {
    case e: BankAccountFieldValidationFailure =>
      fieldErrors("accountNumber") = e
    case _ =>
      fieldErrors("accountNumber") = BankAccountFieldValidationFailure(
        "accountNumber preconditions failed with unknown failure",
        None.orNull,
        BankAccountFailureReason.InvalidBankAccount,
      )
  }

  if (!fieldErrors.isEmpty)
    val fieldErrorDescription = fieldErrors
      .map((fieldName, error) => s"$fieldName: \"${error.message}\"")
      .mkString("\n")

    throw BankAccountValidationFailure(
      s"One or more fields are invalid:\n\n$fieldErrorDescription",
      None.orNull,
      BankAccountFailureReason.InvalidBankAccount,
      fieldErrors,
    )

  private[this] def precondition(
      test: Boolean,
      message: String,
      validationMessage: BankAccountFailureReason,
  ): Unit =
    if (!test)
      throw BankAccountFieldValidationFailure(
        message,
        None.orNull,
        validationMessage,
      )

  private def routingNumberChecksum(routingNumber: String): Int =
    routingNumber
      .grouped(3)
      .foldLeft(0)((sum, triplet) =>
        sum + (
          // NOTE: toString needed to ensure character is parsed rather than cast directly to an int
          triplet(0).toString().toInt * 3
        )
          + (triplet(1).toString().toInt * 7)
          + triplet(2).toString().toInt,
      )
