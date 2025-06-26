package gov.irs.factgraph.types
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.matching.Regex
import gov.irs.factgraph.monads.JSEither
import scala.util.{Try, Success, Failure}

object PinFactory:
  private val PinPattern: Regex = """^([0-9]{5})$""".r

  @JSExportTopLevel("PinFactory")
  def applyPin(
      s: String,
  ): JSEither[PinValidationFailure, Pin] = s match
    case PinPattern(pin) =>
      Try(new Pin(pin)) match
        case Success(v)                       => JSEither.Right(v)
        case Failure(e: PinValidationFailure) => JSEither.Left(e)
        case Failure(exception) =>
          JSEither.Left(
            PinValidationFailure(
              "Invalid PIN case 1",
              None.orNull,
              PinFailureReason.InvalidPin,
            ),
          )
    case _ =>
      JSEither.Left(
        PinValidationFailure(
          "Invalid PIN case 2",
          None.orNull,
          PinFailureReason.InvalidPin,
        ),
      )
