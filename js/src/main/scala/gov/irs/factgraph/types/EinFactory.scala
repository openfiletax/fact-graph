package gov.irs.factgraph.types
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.matching.Regex
import gov.irs.factgraph.monads.JSEither
import scala.util.{Try, Success, Failure}

object EinFactory:
  private val EinPattern: Regex = """^([0-9]{2})([0-9]{7})$""".r

  @JSExportTopLevel("EinFactory")
  def applyEin(
      s: String,
  ): JSEither[EinValidationFailure, Ein] = s match
    case EinPattern(prefix, serial) =>
      Try(new Ein(prefix, serial)) match
        case Success(v)                       => JSEither.Right(v)
        case Failure(e: EinValidationFailure) => JSEither.Left(e)
        case Failure(exception) =>
          JSEither.Left(
            EinValidationFailure(
              "Invalid EIN case 1",
              None.orNull,
              EinFailureReason.InvalidEin,
            ),
          )
    case _ =>
      JSEither.Left(
        EinValidationFailure(
          "Invalid EIN case 2",
          None.orNull,
          EinFailureReason.InvalidEin,
        ),
      )
