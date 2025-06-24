package gov.irs.factgraph.types
import upickle.default.ReadWriter

import java.lang.Enum
import scala.beans.BeanProperty
import scala.scalajs.js.annotation.{JSExportTopLevel, JSExport, JSExportAll}
import scala.scalajs.js

import gov.irs.factgraph.validation.{ValidationFailure, ValidationFailureReason}
import scala.util.matching.Regex
import gov.irs.factgraph.compnodes.Add

@JSExportAll
enum UserFriendlyAddressFailureReason:
  case InvalidAddress
  case InvalidCity
  case InvalidStateFormat
  case InvalidZipCodeFormat

@JSExportAll
enum AddressFailureReason extends Enum[AddressFailureReason], ValidationFailureReason:
  type UserFriendlyReason = UserFriendlyAddressFailureReason

  case RequiredField
  // There is one case for the Address Length
  case InvalidStreetLength
  case InvalidStreetChars
  case InvalidStreetLine2Length
  case InvalidStreetUnknownFailure

  // There are 3 error case for the city:
  case InvalidCityLength
  case InvalidCityChars
  case InvalidCityBasedOnState
  case InvalidCity
  case InvalidCityUnknownFailure

  // There is 1 error case for the state
  case InvalidStateFormat
  case InvalidStateBasedOnCity
  case InvalidStateUnknownFailure

  // There are 2 error cases for the zip code:
  case InvalidZipCodeFormat
  case InvalidZipCodeUknownFailure

  case InvalidAddress

  def toUserFriendlyReason() = this match
    case (InvalidStreetLength | InvalidStreetChars) =>
      UserFriendlyAddressFailureReason.InvalidAddress
    case (InvalidCityLength | InvalidCityChars | InvalidCityBasedOnState) =>
      UserFriendlyAddressFailureReason.InvalidCity
    case (InvalidStateBasedOnCity) =>
      UserFriendlyAddressFailureReason.InvalidStateFormat
    case (InvalidZipCodeFormat) =>
      UserFriendlyAddressFailureReason.InvalidZipCodeFormat
    case _ => UserFriendlyAddressFailureReason.InvalidAddress

@JSExportAll
final case class AddressFieldValidationFailure(
    val message: String = "",
    private val cause: Throwable = None.orNull,
    val validationMessage: AddressFailureReason,
) extends IllegalArgumentException(message, cause),
      ValidationFailure[AddressFailureReason];

@JSExportAll
final case class AddressValidationFailure(
    val message: String = "",
    private val cause: Throwable = None.orNull,
    val validationMessage: AddressFailureReason,
    val addressErrors: collection.Map[String, AddressFieldValidationFailure] = collection.Map(),
) extends IllegalArgumentException(message, cause),
      ValidationFailure[AddressFailureReason];

@JSExportTopLevel("Address")
@JSExportAll
case class Address(
    @BeanProperty streetAddress: String,
    @BeanProperty city: String,
    @BeanProperty postalCode: String,
    @BeanProperty stateOrProvence: String,
    @BeanProperty streetAddressLine2: String = "",
    @BeanProperty country: String = "United States of America",
) derives ReadWriter:
  // If you need to add a new field to the address type, you'll need to add it above and then
  // also put it in addressFields so we know what order the fields go in
  // You'll also want to check .toString() to make sure it goes to the frontend formatted correctly
  private val addressFields =
    List(
      streetAddress,
      streetAddressLine2,
      city,
      postalCode,
      stateOrProvence,
      country,
    )

  private val MilitaryPosts: List[String] = List("AA", "AE", "AP")
  private val MilitaryCities: List[String] = List("APO", "DPO", "FPO")

  val StreetPattern: Regex = """[A-Za-z0-9]( ?[A-Za-z0-9\-/])*""".r
  val StreetPatternLength: Regex = """[a-zA-Z0-9\-\/ ]{1,35}""".r
  val CityPattern: Regex = """^([A-Za-z ])+$""".r
  val StatePattern: Regex = """[A-Z]{2}""".r
  val ZipCodePattern: Regex = """(\d{5})|(\d{5}-\d{4})|(\d{5}-\d{7})""".r

  var addressErrors =
    collection.mutable.Map[String, AddressFieldValidationFailure]()

  // try/catch for streets line1
  try {
    precondition(
      !streetAddress.isEmpty(),
      "Address field is required",
      AddressFailureReason.RequiredField,
    )
    precondition(
      StreetPattern.matches(streetAddress),
      "Address field is using unallowed chars",
      AddressFailureReason.InvalidStreetChars,
    )
    precondition(
      StreetPatternLength.matches(streetAddress),
      "StreetLine1 is above 35 characters",
      AddressFailureReason.InvalidStreetLength,
    )
  } catch {
    case e: AddressFieldValidationFailure => addressErrors("streetAddress") = e
    case _ =>
      addressErrors("street") = AddressFieldValidationFailure(
        "AddressStreet preconditions failed with unknown failure",
        None.orNull,
        AddressFailureReason.InvalidStreetUnknownFailure,
      )
  }
  // try/catch for streets line2
  try {
    if (!streetAddressLine2.isEmpty)
      precondition(
        StreetPattern.matches(streetAddressLine2),
        "Address field is using unallowed chars",
        AddressFailureReason.InvalidStreetChars,
      )
      // TODO: Allow 35 character limit to be configured as the cumulative total or individually
      if (!streetAddressLine2.isEmpty)
        precondition(
          StreetPatternLength.matches(streetAddressLine2),
          "StreetLine2 is above 35 characters",
          AddressFailureReason.InvalidStreetLine2Length,
        )
  } catch {
    case e: AddressFieldValidationFailure =>
      addressErrors("streetAddressLine2") = e
    case _ =>
      addressErrors("street") = AddressFieldValidationFailure(
        "AddressStreetLine2 preconditions failed with unknown failure",
        None.orNull,
        AddressFailureReason.InvalidStreetUnknownFailure,
      )
  }

  // try/catch city preconditions
  try {
    precondition(
      !city.isEmpty(),
      "Address field is required",
      AddressFailureReason.RequiredField,
    )
    precondition(
      CityPattern.matches(city),
      "City field is using unallowed chars only [A-Za-z/s]",
      AddressFailureReason.InvalidCityChars,
    )
    // The 22 character requirement is for US Addresses and can be seen on line 689 here:
    // file://./../../../../../../../../../mef-tests/src/main/resources/xsd/2022v5.2/Common/efileTypes.xsd#L689
    precondition(
      city.length >= 3 && city.length <= 22,
      "City field is of incorrect length",
      AddressFailureReason.InvalidCityLength,
    )

    if (MilitaryPosts.contains(stateOrProvence)) {
      precondition(
        MilitaryCities.contains(city),
        "Invalid city based on state",
        AddressFailureReason.InvalidCityBasedOnState,
      )
    }
  } catch {
    case e: AddressFieldValidationFailure => addressErrors("city") = e
    case _ =>
      addressErrors("city") = AddressFieldValidationFailure(
        "City preconditions failed with unknown failure",
        None.orNull,
        AddressFailureReason.InvalidCityUnknownFailure,
      )
  }

  // try/catch for state
  try {
    precondition(
      !stateOrProvence.isEmpty(),
      "Address field is required",
      AddressFailureReason.RequiredField,
    )
    precondition(
      StatePattern.matches(stateOrProvence),
      "Invalid state",
      AddressFailureReason.InvalidStateFormat,
    )
    if (MilitaryCities.contains(city)) {
      precondition(
        MilitaryPosts.contains(stateOrProvence),
        "Invalid state based on city",
        AddressFailureReason.InvalidStateBasedOnCity,
      )
    }
  } catch {
    case e: AddressFieldValidationFailure =>
      addressErrors("stateOrProvence") = e
    case _ =>
      addressErrors("stateOrProvence") = AddressFieldValidationFailure(
        "State preconditions failed with unknown failure",
        None.orNull,
        AddressFailureReason.InvalidStateUnknownFailure,
      )
  }

  // try/catch on zip code preconditions
  try {
    precondition(
      !postalCode.isEmpty(),
      "Address field is required",
      AddressFailureReason.RequiredField,
    )
    precondition(
      ZipCodePattern.matches(postalCode),
      "Zip/postal code does not meet expected format",
      AddressFailureReason.InvalidZipCodeFormat,
    )

  } catch {
    case e: AddressFieldValidationFailure => addressErrors("postalCode") = e
    case _ =>
      addressErrors("postalCode") = AddressFieldValidationFailure(
        "Zip/postal code preconditions failed with unknown failure",
        None.orNull,
        AddressFailureReason.InvalidZipCodeUknownFailure,
      )
  }

  if (!addressErrors.isEmpty)
    // println(addressErrors("city").message)
    throw AddressValidationFailure(
      "One or more address fields in error",
      None.orNull,
      AddressFailureReason.InvalidAddress,
      addressErrors,
    )

  // Attempted to add this method however neither source is accurate:
  // - https://www.irs.gov/pub/irs-utl/zip_code_and_state_abbreviations.pdf
  // - https://www.structnet.com/instructions/zip_min_max_by_state.html
  // ZIP code will not be validated without API access

  def foreignAddress(): Boolean =
    country != "United States of America"

  override def toString(): String =
    val streetLines =
      List(streetAddress, streetAddressLine2).filter(_ != "").mkString("\n")
    s"$streetLines\n$city, $stateOrProvence $postalCode\n$country"

  private[this] def precondition(
      test: Boolean,
      message: String,
      validationMessage: AddressFailureReason,
  ): Unit =
    if (!test)
      throw AddressFieldValidationFailure(
        message,
        None.orNull,
        validationMessage,
      )

object Address:
  def apply(s: String): Address =
    val addressLines = s.split("\n")
    val streetAddress = addressLines(0)
    val Array(city, stateAndPostalCode) =
      addressLines(addressLines.length - 1).split(',')
    val Array(state, postalCode) = stateAndPostalCode.trim().split(' ')
    val country = "United States of America"
    addressLines.length match
      case 2 =>
        new Address(
          streetAddress,
          city,
          postalCode,
          state,
          "",
          country,
        )
      case 3 =>
        new Address(
          streetAddress,
          city,
          postalCode,
          state,
          addressLines(1),
          country,
        )
      case _: Int =>
        throw AddressValidationFailure(
          "One or more address fields in error",
          None.orNull,
          AddressFailureReason.InvalidAddress,
        )
