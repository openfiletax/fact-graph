package gov.irs.factgraph.types
import upickle.default.ReadWriter

import java.lang.Enum
import scala.beans.BeanProperty
import scala.compiletime.ops.boolean
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportAll
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.matching.Regex

import gov.irs.factgraph.validation.{ValidationFailure, ValidationFailureReason}

@JSExportAll
enum UserFriendlyEinFailureReason:
  case InvalidEin
  case InvalidPrefix

@JSExportAll
enum EinFailureReason extends Enum[EinFailureReason], ValidationFailureReason:
  type UserFriendlyReason = UserFriendlyEinFailureReason
  case InvalidPrefixLength
  case InvalidSerialLength
  case InvalidChars
  case InvalidLength
  case InvalidEin
  case InvalidPrefix

  def toUserFriendlyReason() = this match
    case (
          InvalidPrefixLength | InvalidSerialLength | InvalidChars | InvalidLength | InvalidEin
        ) =>
      UserFriendlyEinFailureReason.InvalidEin
    case InvalidPrefix => UserFriendlyEinFailureReason.InvalidPrefix

@JSExportAll
final case class EinValidationFailure(
    private val message: String = "",
    private val cause: Throwable = None.orNull,
    val validationMessage: EinFailureReason,
) extends IllegalArgumentException(message, cause),
      ValidationFailure[EinFailureReason];

@JSExportTopLevel("Ein")
@JSExportAll
final case class Ein(
    @BeanProperty prefix: String,
    @BeanProperty serial: String,
) derives ReadWriter:

  // https://www.irs.gov/businesses/small-businesses-self-employed/how-eins-are-assigned-and-valid-ein-prefixes
  private val invalidPrefixes = List(
    "07",
    "08",
    "09",
    "17",
    "18",
    "19",
    "28",
    "29",
    "49",
    "69",
    "70",
    "78",
    "79",
    "89",
    "96",
    "97",
  )

  precondition(
    prefix.length == 2,
    "Prefix of wrong length",
    EinFailureReason.InvalidPrefixLength,
  )
  precondition(
    serial.length == 7,
    "Serial of wrong length",
    EinFailureReason.InvalidSerialLength,
  )
  precondition(
    List(prefix, serial).forall(arg => arg.forall(_.isDigit)),
    "Numbers only",
    EinFailureReason.InvalidChars,
  )
  precondition(
    !invalidPrefixes.contains(prefix),
    "valid prefixes only",
    EinFailureReason.InvalidPrefix,
  )

  override def toString(): String = s"${prefix}-${serial}"

  private[this] def precondition(
      test: Boolean,
      message: String,
      validationMessage: EinFailureReason,
  ): Unit =
    if (!test)
      throw EinValidationFailure(message, None.orNull, validationMessage)

object Ein:
  private val Pattern: Regex = raw"([0-9]{2})([0-9]{7})".r

  def apply(s: String): Ein = this.parseString(s) match
    case Some(tin) => tin
    case None =>
      throw EinValidationFailure(
        "TINs must be 9 digits long",
        None.orNull,
        EinFailureReason.InvalidLength,
      )

  @JSExport
  def parseString(s: String): Option[Ein] =
    val cleanInput = s.replaceAll("[^0-9]", "")
    cleanInput match
      case Pattern(prefix, serial) => Some(this(prefix, serial))
      case _                       => None
