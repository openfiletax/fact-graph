package gov.irs.factgraph.types

import java.lang.{Enum => JavaEnum}
import scala.scalajs.js.annotation.{JSExport, JSExportAll, JSExportTopLevel}
import scala.util.matching.Regex
import scala.util.{Try, Success, Failure}

import gov.irs.factgraph.monads.JSEither
import gov.irs.factgraph.validation.{ValidationFailure, ValidationFailureReason}

// NOTE: These classes and types are specified just to simplify some front-end logstics
// This pattern may need some reconsideration to minimize boilerplate

@JSExportAll
enum EnumFailureReason extends JavaEnum[EnumFailureReason], ValidationFailureReason:
  type UserFriendlyReason = EnumFailureReason
  case BlankEnum
  case InvalidEnum

  def toUserFriendlyReason() =
    this // Error reason is already user friendly

@JSExportAll
final case class EnumValidationFailure(
    private val message: String = "",
    private val cause: Throwable = None.orNull,
    val validationMessage: EnumFailureReason,
) extends IllegalArgumentException(message, cause),
      ValidationFailure[EnumFailureReason];

object EnumFactory:
  @JSExportTopLevel("EnumFactory")
  def apply(
      value: String,
      enumOptionsPath: String,
  ): JSEither[EnumValidationFailure, Enum] =
    if (value.length > 0) {
      Try(new Enum(Some(value), enumOptionsPath)) match
        case Success(v) => JSEither.Right(v)
        case Failure(exception) =>
          JSEither.Left(
            EnumValidationFailure(
              "Failed to initialize enum",
              None.orNull,
              EnumFailureReason.InvalidEnum,
            ),
          )
    } else {
      JSEither.Left(
        EnumValidationFailure(
          "Blank Enum",
          None.orNull,
          EnumFailureReason.InvalidEnum,
        ),
      )
    }
