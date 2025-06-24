package gov.irs.factgraph.types
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.matching.Regex
import gov.irs.factgraph.monads.JSEither
import scala.util.{Try, Success, Failure}

object PhoneNumberFactory:
  private val UsPhonePattern: Regex = """^\+1(\d{3})(\d{3})(\d{4})$""".r
  private val E164Pattern: Regex = """^\+([1-9]{1,3})(\d{1,14})$""".r

  @JSExportTopLevel("UsPhoneNumberFactory")
  def applyUsPhoneNumber(
      s: String,
  ): JSEither[E164NumberValidationFailure, UsPhoneNumber] = s match
    case UsPhonePattern(area, office, line) =>
      Try(new UsPhoneNumber(area, office, line)) match
        case Success(v)                              => JSEither.Right(v)
        case Failure(e: E164NumberValidationFailure) => JSEither.Left(e)
        case Failure(exception) =>
          JSEither.Left(
            E164NumberValidationFailure(
              "Malformed US Phone Number",
              None.orNull,
              E164NumberFailureReason.MalformedPhoneNumber,
            ),
          )
    case _ =>
      JSEither.Left(
        E164NumberValidationFailure(
          "Malformed US Phone Number",
          None.orNull,
          E164NumberFailureReason.MalformedPhoneNumber,
        ),
      )

  @JSExportTopLevel("InternationalPhoneNumberFactory")
  def applyInternationalPhoneNumber(
      s: String,
  ): JSEither[E164NumberValidationFailure, InternationalPhoneNumber] = s match
    case E164Pattern(country, subscriber) =>
      Try(new InternationalPhoneNumber(country, subscriber)) match
        case Success(v)                              => JSEither.Right(v)
        case Failure(e: E164NumberValidationFailure) => JSEither.Left(e)
        case Failure(exception) =>
          JSEither.Left(
            E164NumberValidationFailure(
              "Malformed International Phone Number",
              None.orNull,
              E164NumberFailureReason.MalformedPhoneNumber,
            ),
          )
    case _ =>
      JSEither.Left(
        E164NumberValidationFailure(
          "Malformed International Phone Number",
          None.orNull,
          E164NumberFailureReason.MalformedPhoneNumber,
        ),
      )
