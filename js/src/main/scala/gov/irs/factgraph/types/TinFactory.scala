package gov.irs.factgraph.types
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.matching.Regex
import gov.irs.factgraph.monads.JSEither
import scala.util.{Try, Success, Failure}

object TinFactory:
  private val TinPattern: Regex = """^([0-9]{3})([0-9]{2})([0-9]{4})$""".r

  @JSExportTopLevel("TinFactory")
  def applyTin(
      s: String,
      allowAllZeros: Boolean = false,
  ): JSEither[TinValidationFailure, Tin] = s match
    case TinPattern(area, group, serial) =>
      Try(new Tin(area, group, serial, allowAllZeros)) match
        case Success(v)                       => JSEither.Right(v)
        case Failure(e: TinValidationFailure) => JSEither.Left(e)
        case Failure(exception) =>
          JSEither.Left(
            TinValidationFailure(
              "Invalid TIN case 1",
              None.orNull,
              TinFailureReason.InvalidTin,
            ),
          )
    case _ =>
      JSEither.Left(
        TinValidationFailure(
          "Invalid TIN case 2",
          None.orNull,
          TinFailureReason.InvalidTin,
        ),
      )
