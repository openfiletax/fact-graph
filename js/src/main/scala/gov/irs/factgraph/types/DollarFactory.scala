package gov.irs.factgraph.types

import java.lang.Enum
import scala.scalajs.js.annotation.{JSExport, JSExportAll, JSExportTopLevel}
import scala.util.matching.Regex
import scala.util.{Try, Success, Failure}

import gov.irs.factgraph.monads.JSEither
import gov.irs.factgraph.validation.{ValidationFailure, ValidationFailureReason}

import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.{Try, Success, Failure}
import scala.scalajs.js

object DollarFactory:
  @JSExportTopLevel("DollarFactory")
  def apply(
      value: String,
      maxLimit: js.UndefOr[Double] = js.undefined,
      minLimit: js.UndefOr[Double] = js.undefined,
  ): JSEither[DollarValidationFailure, Dollar] =
    // maxLimit is not defined or numericValue doesn't exceed the maxlimit
    Try(Dollar(value, allowRounding = false)) match
      case Success(v) =>
        if (maxLimit.isDefined && v > maxLimit.get) {
          // value exceeds limit
          return JSEither.Left(
            DollarValidationFailure(
              "Value exceeds max limit",
              None.orNull,
              DollarFailureReason.ExceedsMaxLimit,
            ),
          )
        } else if (minLimit.isDefined && v < minLimit.get) {
          // value exceeds limit
          return JSEither.Left(
            DollarValidationFailure(
              "Value exceeds min limit",
              None.orNull,
              DollarFailureReason.ExceedsMinLimit,
            ),
          )
        } else {
          JSEither.Right(v)
        }
      case Failure(e: DollarValidationFailure) => JSEither.Left(e)
      case Failure(exception) =>
        JSEither.Left(
          DollarValidationFailure(
            "Failed to parse Dollar",
            exception,
            DollarFailureReason.InvalidDollar,
          ),
        )
