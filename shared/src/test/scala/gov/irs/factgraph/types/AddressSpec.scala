package gov.irs.factgraph.types

import org.scalatest.funspec.AnyFunSpec

class AddressSpec extends AnyFunSpec:
  describe("Address") {
    describe(".toString") {
      it("Converts a simple address to a standard English-formatted address") {
        val address = new Address(
          streetAddress = "736 Jackson Place NW",
          city = "Washington",
          stateOrProvence = "DC",
          postalCode = "20501"
        )
        assert(
          address
            .toString() == "736 Jackson Place NW\nWashington, DC 20501\nUnited States of America"
        )
      }
      it(
        "Converts an address with a second line to a standard English-formatted address"
      ) {
        val address = new Address(
          streetAddress = "736 Jackson Place NW",
          streetAddressLine2 = "1st floor",
          city = "Washington",
          stateOrProvence = "DC",
          postalCode = "20501"
        )
        assert(
          address
            .toString() == "736 Jackson Place NW\n1st floor\nWashington, DC 20501\nUnited States of America"
        )
      }
    }
    describe("$apply") {
      it("can parse a single address line") {
        val expectedAddress = new Address(
          streetAddress = "736 Jackson Place NW",
          city = "Washington",
          stateOrProvence = "DC",
          postalCode = "20501"
        )
        assert(
          expectedAddress == Address(
            "736 Jackson Place NW\nWashington, DC 20501"
          )
        )
        assert(
          Address(
            "736 Jackson Place NW\nWashington, DC 20501"
          ).postalCode == "20501"
        )
      }
      it("can parse a multi-line address") {
        val expectedAddress = new Address(
          streetAddress = "736 Jackson Place NW",
          city = "Washington",
          stateOrProvence = "DC",
          streetAddressLine2 = "812W",
          postalCode = "20501"
        )
        assert(
          expectedAddress == Address(
            "736 Jackson Place NW\n812W\nWashington, DC 20501"
          )
        )
        assert(
          Address(
            "736 Jackson Place NW\n812W\nWashington, DC 20501"
          ).postalCode == "20501"
        )
      }
    }
    describe("preconditions for the street fields") {
      it("throws an error because street is required") {
        try {
          new Address(
            streetAddress = "",
            city = "Washington",
            stateOrProvence = "DC",
            postalCode = "20507"
          )
        } catch {
          case e: AddressValidationFailure => {
            assert(
              e.addressErrors("streetAddress")
                .validationMessage
                .toString() == "RequiredField"
            )
          }
        }
      }
      it("street line1 is in error due to invalid chars") {
        try {
          new Address(
            streetAddress = "736 Jackson Place NW $",
            city = "Washington",
            stateOrProvence = "DC",
            postalCode = "20507"
          )
        } catch {
          case e: AddressValidationFailure => {
            assert(
              e.addressErrors("streetAddress")
                .validationMessage
                .toString() == "InvalidStreetChars"
            )
          }
        }
      }
      it("street line1 is in error due to going above 35 chars") {
        try {
          new Address(
            streetAddress = "abcdefghijklmnopqrstuvqxyz1234567890",
            city = "Washington",
            stateOrProvence = "DC",
            postalCode = "20507"
          )
        } catch {
          case e: AddressValidationFailure => {
            assert(
              e.addressErrors("streetAddress")
                .validationMessage
                .toString() == "InvalidStreetLength"
            )
          }
        }
      }
      ignore("street line2 is in error due to invalid chars") {
        try {
          new Address(
            streetAddress = "736 Jackson Place NW ",
            streetAddressLine2 = "Apt 23$",
            city = "Washington",
            stateOrProvence = "DC",
            postalCode = "20507"
          )
        } catch {
          case e: AddressValidationFailure => {
            assert(
              e.addressErrors("streetAddress")
                .validationMessage
                .toString() == "InvalidStreetChars"
            )
          }
        }
      }
      it(
        "ensures that even with errors in line1 it doesn't trigger line2 error"
      ) {
        try {
          new Address(
            streetAddress = "123 E. Main",
            streetAddressLine2 = "Apt 23",
            city = "Washington",
            stateOrProvence = "DC",
            postalCode = "20507"
          )
        } catch {
          case e: AddressValidationFailure => {
            assert(
              e.addressErrors("streetAddress")
                .validationMessage
                .toString() == "InvalidStreetChars"
            )
            assert(e.addressErrors.size == 1)
          }
        }
      }
      ignore("street line1+line2 is in error due to invalid chars") {
        try {
          new Address(
            streetAddress = "abcdejghijklmnopqrstuvwxyz",
            streetAddressLine2 = "0123456789",
            city = "Washington",
            stateOrProvence = "DC",
            postalCode = "20507"
          )
        } catch {
          case e: AddressValidationFailure => {
            assert(
              e.addressErrors("streetAddress")
                .validationMessage
                .toString() == "InvalidTotalStreetChars"
            )
          }
        }
      }
    }
    describe("preconditions for the city field") {
      it("disallows special characters in city") {
        try {
          new Address(
            streetAddress = "736 Jackson Place NW",
            city = "Washington!",
            stateOrProvence = "DC",
            postalCode = "20504"
          )
        } catch {
          case e: AddressValidationFailure => {
            assert(
              e.addressErrors("city")
                .validationMessage
                .toString() == "InvalidCityChars"
            )
          }
        }
      }
      it("throws an error because city is requried") {
        try {
          new Address(
            streetAddress = "736 Jackson Place NW",
            city = "",
            stateOrProvence = "DC",
            postalCode = "20504"
          )
        } catch {
          case e: AddressValidationFailure => {
            assert(
              e.addressErrors("city")
                .validationMessage
                .toString() == "RequiredField"
            )
          }
        }
      }
      it("throws an error when city is less than 3 characaters") {
        val e = intercept[AddressValidationFailure] {
          Address(
            streetAddress = "736 Jackson Place NW",
            city = "Hi",
            stateOrProvence = "DC",
            postalCode = "20505"
          )
        }

        assert(
          e.addressErrors("city")
            .validationMessage == AddressFailureReason.InvalidCityLength
        )
      }
      it("throws an error when city is more than 22 characaters") {
        val e = intercept[AddressValidationFailure] {
          Address(
            streetAddress = "736 Jackson Place NW",
            city = "Hi",
            stateOrProvence = "DC",
            postalCode = "20505"
          )
        }

        assert(
          e.addressErrors("city")
            .validationMessage == AddressFailureReason.InvalidCityLength
        )
      }

      it("allows 22 characters in city") {
        new Address(
          streetAddress = "736 Jackson Place NW",
          city = "WashingtonWashingtonWa",
          stateOrProvence = "DC",
          postalCode = "20505"
        )
      }
      it("allows 2 word cities") {
        new Address(
          streetAddress = "736 Jackson Place NW",
          city = "New York",
          stateOrProvence = "DC",
          postalCode = "20505"
        )
      }
      it("allows 3 word cities") {
        new Address(
          streetAddress = "736 Jackson Place NW",
          city = "San Fernando Valley",
          stateOrProvence = "DC",
          postalCode = "20505"
        )
      }
      it("allows only specific cities if state is miliary") {
        try {
          new Address(
            streetAddress = "736 Jackson Place NW",
            city = "Washington",
            stateOrProvence = "AA",
            postalCode = "20506"
          )
        } catch {
          case e: AddressValidationFailure => {
            assert(
              e.addressErrors("city")
                .validationMessage
                .toString() == "InvalidCityBasedOnState"
            )
          }
        }
      }
    }
    describe("preconditions for the state field") {
      it("throws an error because state is requried") {
        try {
          new Address(
            streetAddress = "736 Jackson Place NW",
            city = "Washington",
            stateOrProvence = "",
            postalCode = "20504"
          )
        } catch {
          case e: AddressValidationFailure => {
            assert(
              e.addressErrors("stateOrProvence")
                .validationMessage
                .toString() == "RequiredField"
            )
          }
        }
      }
      it("state is not two letters") {
        try {
          new Address(
            streetAddress = "736 Jackson Place NW",
            city = "APO",
            stateOrProvence = "Colorado",
            postalCode = "20507"
          )
        } catch {
          case e: AddressValidationFailure => {
            assert(
              e.addressErrors("stateOrProvence")
                .validationMessage
                .toString() == "InvalidStateFormat"
            )
          }
        }
      }
      it("state is error based on the city") {
        try {
          new Address(
            streetAddress = "736 Jackson Place NW",
            city = "APO",
            stateOrProvence = "CO",
            postalCode = "20507"
          )
        } catch {
          case e: AddressValidationFailure => {
            assert(
              e.addressErrors("stateOrProvence")
                .validationMessage
                .toString() == "InvalidStateBasedOnCity"
            )
          }
        }
      }
    }
    describe("preconditions for the zip/postal code field") {
      it("throws an error because zip is requried") {
        try {
          new Address(
            streetAddress = "736 Jackson Place NW",
            city = "Washington",
            stateOrProvence = "DC",
            postalCode = ""
          )
        } catch {
          case e: AddressValidationFailure => {
            assert(
              e.addressErrors("postalCode")
                .validationMessage
                .toString() == "RequiredField"
            )
          }
        }
      }
      it("zip/postal code is not in the standard 5 digit format ") {
        try {
          new Address(
            streetAddress = "736 Jackson Place NW",
            city = "Dallas",
            stateOrProvence = "TX",
            postalCode = "203"
          )
        } catch {
          case e: AddressValidationFailure => {
            assert(
              e.addressErrors("postalCode")
                .validationMessage
                .toString() == "InvalidZipCodeFormat"
            )
          }
        }
      }
      it("zip/postal code is not in the standard 5+4 digit format") {
        try {
          new Address(
            streetAddress = "736 Jackson Place NW",
            city = "Dallas",
            stateOrProvence = "TX",
            postalCode = "203-098"
          )
        } catch {
          case e: AddressValidationFailure => {
            assert(
              e.addressErrors("postalCode")
                .validationMessage
                .toString() == "InvalidZipCodeFormat"
            )
          }
        }
      }
    }
  }
