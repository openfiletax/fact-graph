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

// NOTE: We currently only have one user-facing error for this field,
//    but the enum and helper are being created to preserve parity with
//    the pattern established in PhoneNumber.scala
@JSExportAll
enum UserFriendlyTinFailureReason:
  case InvalidTin

@JSExportAll
enum TinFailureReason extends Enum[TinFailureReason], ValidationFailureReason:
  type UserFriendlyReason = UserFriendlyTinFailureReason
  case InvalidAreaLength
  case InvalidGroupLength
  case InvalidSerialLength
  case InvalidChars
  case InvalidLength
  case InvalidTin
  case InvalidSSN

  def toUserFriendlyReason() = this match
    case (
          InvalidAreaLength | InvalidGroupLength | InvalidSerialLength | InvalidChars | InvalidLength | InvalidTin |
          InvalidSSN
        ) =>
      UserFriendlyTinFailureReason.InvalidTin

@JSExportAll
final case class TinValidationFailure(
    private val message: String = "",
    private val cause: Throwable = None.orNull,
    val validationMessage: TinFailureReason,
) extends IllegalArgumentException(message, cause),
      ValidationFailure[TinFailureReason];

@JSExportTopLevel("Tin")
@JSExportAll
final case class Tin(
    @BeanProperty area: String,
    @BeanProperty group: String,
    @BeanProperty serial: String,
    @BeanProperty allowAllZeros: Boolean = false,
) derives ReadWriter:

  precondition(
    area.length == 3,
    "Area of wrong length",
    TinFailureReason.InvalidAreaLength,
  )
  if (!allowAllZeros) then
    precondition(
      area != "000",
      "Area can not have all zeros",
      TinFailureReason.InvalidTin,
    )
  precondition(
    area != "666",
    "Area can not have all sixes",
    TinFailureReason.InvalidTin,
  )
  precondition(
    group.length == 2,
    "Group of wrong length",
    TinFailureReason.InvalidGroupLength,
  )
  precondition(
    serial.length == 4,
    "Serial of wrong length",
    TinFailureReason.InvalidSerialLength,
  )
  precondition(
    List(area, group, serial).forall(arg => arg.forall(_.isDigit)),
    "Numbers only",
    TinFailureReason.InvalidChars,
  )

  override def toString(): String = s"${area}-${group}-${serial}"

  private[this] def precondition(
      test: Boolean,
      message: String,
      validationMessage: TinFailureReason,
  ): Unit =
    if (!test)
      throw TinValidationFailure(message, None.orNull, validationMessage)

  def isITIN: Boolean =
    // Per IRS Publication 4757 (Rev. 10-2022)
    // https://www.irs.gov/pub/irs-pdf/p4757.pdf
    // All valid ITINs are a nine-digit number in the same format
    // as the SSN (9XX-8X-XXXX), begins with a “9” and the
    // 4th and 5th digits range from 50 to 65, 70 to 88, 90 to 92,
    // and 94 to 99.
    if (area.startsWith("9")) {
      val numericGroup = group.toInt
      if (
        (numericGroup == 0) || // "00" for testing; should be considered an ITIN
        (numericGroup >= 50 && numericGroup <= 65) ||
        (numericGroup >= 70 && numericGroup <= 88) ||
        (numericGroup >= 90 && numericGroup <= 92) ||
        (numericGroup >= 94 && numericGroup <= 99)
      ) {
        true
      } else {
        false
      }
    } else {
      false
    }

  def isSSN: Boolean =
    // Per SSA, an SSN won't ever actually start with 000, 666, or 900-999
    // Additionally, SSN serials are valid between 0001-9999
    // https://www.ssa.gov/employer/randomization.html
    // Previously unassigned area numbers were introduced for assignment excluding area numbers 000, 666 and 900-999.
    // Additionally, if an employee has applied for an SSN but doesn't have one in time for filing, they
    // 000-00-0000, per https://www.irs.gov/instructions/iw2w3#en_US_2024_publink1000308330
    !(area.startsWith(
      "9",
    ) || (serial == "0000")) || (allowAllZeros && area == "000" && group == "00" && serial == "0000")

  def isATIN: Boolean =
    /* Per the IRS manual on ATIN applications,
    an ATIN is a nine-digit number in the same format
    as the SSN, but always beginning with a “9” and the
    4th and 5th digits always "93".
    (9XX-93-XXXX)
    https://www.irs.gov/irm/part3/irm_03-013-040#idm140587214339952
     */
    area.startsWith("9") && group == "93"

object Tin:
  private val Pattern: Regex = raw"([0-9]{3})([0-9]{2})([0-9]{4})".r

  def apply(s: String): Tin =
    this.apply(s, false)

  def apply(s: String, allowAllZeros: Boolean): Tin =
    this.parseString(s, allowAllZeros) match
      case Some(tin) => tin
      case None =>
        throw TinValidationFailure(
          "TINs must be 9 digits long",
          None.orNull,
          TinFailureReason.InvalidLength,
        )

  @JSExport
  def parseString(s: String, allowAllZeros: Boolean = false): Option[Tin] =
    val cleanInput = s.replaceAll("[^0-9]", "")
    cleanInput match
      case Pattern(area, group, serial) =>
        Some(this(area, group, serial, allowAllZeros))
      case _ => None
