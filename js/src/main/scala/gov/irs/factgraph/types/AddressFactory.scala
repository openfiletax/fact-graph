package gov.irs.factgraph.types
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.matching.Regex
import gov.irs.factgraph.monads.JSEither
import scala.util.{Try, Success, Failure}

object AddressFactory:
  @JSExportTopLevel("AddressFactory")
  def apply(
      streetAddress: String,
      city: String,
      postalCode: String,
      stateOrProvence: String,
      streetAddressLine2: String = "",
      country: String = "United States of America",
  ): JSEither[AddressValidationFailure, Address] = List(
    streetAddress,
    streetAddressLine2,
    city,
    postalCode,
    stateOrProvence,
    country,
  ) match
    case List(
          streetAddress: String,
          streetAddressLine2: String,
          city: String,
          postalCode: String,
          stateOrProvence: String,
          country: String,
        ) =>
      Try(
        new Address(
          streetAddress,
          city,
          postalCode,
          stateOrProvence,
          streetAddressLine2,
          country,
        ),
      ) match
        case Success(v)                           => JSEither.Right(v)
        case Failure(e: AddressValidationFailure) => JSEither.Left(e)
        case Failure(exception) =>
          JSEither.Left(
            AddressValidationFailure(
              "Invalid Address case 1",
              None.orNull,
              AddressFailureReason.InvalidAddress,
            ),
          )
    case _ =>
      JSEither.Left(
        AddressValidationFailure(
          "Invalid Address case 2",
          None.orNull,
          AddressFailureReason.InvalidAddress,
        ),
      )

  @JSExportTopLevel("formatAddressForHTML")
  def formatAddressForHTML(addr: Address): String =
    addr.toString().replace("\n", "<br />")
