package gov.irs.factgraph.types
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.matching.Regex
import gov.irs.factgraph.monads.JSEither
import scala.util.{Try, Success, Failure}
import scala.scalajs.js.annotation.{JSExport, JSExportAll, JSExportTopLevel}
import java.time.format.DateTimeParseException
import java.lang.{Enum => JavaEnum}
import gov.irs.factgraph.validation.{ValidationFailure, ValidationFailureReason}
import scala.scalajs.js

@JSExportAll
enum DayFailureReason extends JavaEnum[DayFailureReason], ValidationFailureReason:
  type UserFriendlyReason = DayFailureReason
  case InvalidDayDueToLeapYear
  case InvalidDay
  case InvalidMonth
  case InvalidDate
  case ExceedsMaxLimit
  case InvalidLimit

  def toUserFriendlyReason() =
    this // Error reason is already user friendly

@JSExportAll
final case class DayValidationFailure(
    private val message: String = "",
    private val cause: Throwable = None.orNull,
    val validationMessage: DayFailureReason,
) extends IllegalArgumentException(message, cause),
      ValidationFailure[DayFailureReason];

object DayFactory:

  def checkMax(
      inputDay: Day,
      max: js.UndefOr[String] = js.undefined,
  ): JSEither[DayValidationFailure, Day] =
    var maxDay = Day(max.toOption)

    maxDay match
      case Some(v) =>
        if (inputDay >= v) {
          JSEither.Left(
            DayValidationFailure(
              "Max date limit exceeded",
              None.orNull,
              DayFailureReason.ExceedsMaxLimit,
            ),
          )
        } else {
          JSEither.Right(inputDay)
        }
      case None if max.isDefined =>
        JSEither.Left(
          DayValidationFailure(
            "Invalid limit set",
            None.orNull,
            DayFailureReason.InvalidLimit,
          ),
        )
      case None =>
        JSEither.Right(inputDay)

  @JSExportTopLevel("DayFactory")
  def apply(
      s: String,
      max: js.UndefOr[String] = js.undefined,
  ): JSEither[DayValidationFailure, Day] =
    Try(Day(s)) match
      case Success(v) =>
        checkMax(v, max)
      case Failure(e: DateTimeParseException) =>
        if (e.getMessage().contains("MonthOfYear"))
          JSEither.Left(
            DayValidationFailure(
              "Invalid Month: out of range (1-12)",
              None.orNull,
              DayFailureReason.InvalidMonth,
            ),
          )
        else if (e.getMessage().contains("DayOfMonth"))
          JSEither.Left(
            DayValidationFailure(
              "Invalid Day: out of range (1-28/31)",
              None.orNull,
              DayFailureReason.InvalidDay,
            ),
          )
        else if (e.getMessage().contains("leap year"))
          JSEither.Left(
            DayValidationFailure(
              "Invalid Day due to leap year",
              None.orNull,
              DayFailureReason.InvalidDayDueToLeapYear,
            ),
          )
        else
          JSEither.Left(
            DayValidationFailure(
              "Invalid Date: invalid day given month",
              None.orNull,
              DayFailureReason.InvalidDay,
            ),
          )
      case _ =>
        JSEither.Left(
          DayValidationFailure(
            "Invalid Date",
            None.orNull,
            DayFailureReason.InvalidDate,
          ),
        )
