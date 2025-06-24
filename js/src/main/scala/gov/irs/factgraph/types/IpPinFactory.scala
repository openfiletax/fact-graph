package gov.irs.factgraph.types
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.matching.Regex
import gov.irs.factgraph.monads.JSEither
import scala.util.{Try, Success, Failure}

object IpPinFactory:
  private val IpPinPattern: Regex = """^([0-9]{6})$""".r

  @JSExportTopLevel("IpPinFactory")
  def applyIpPin(
      s: String,
  ): JSEither[IpPinValidationFailure, IpPin] = s match
    case IpPinPattern(ippin) =>
      Try(new IpPin(ippin)) match
        case Success(v)                         => JSEither.Right(v)
        case Failure(e: IpPinValidationFailure) => JSEither.Left(e)
        case Failure(exception) =>
          JSEither.Left(
            IpPinValidationFailure(
              "Invalid IP PIN case 1",
              None.orNull,
              IpPinFailureReason.InvalidIpPin,
            ),
          )
    case _ =>
      JSEither.Left(
        IpPinValidationFailure(
          "Invalid IP PIN case 2",
          None.orNull,
          IpPinFailureReason.InvalidIpPin,
        ),
      )
