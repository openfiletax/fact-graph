package gov.irs.factgraph.types

import org.scalatest.funspec.AnyFunSpec

class PhoneNumberSpec extends AnyFunSpec:
  describe("InternationalPhoneNumber") {
    describe("constructor") {
      it("allows country codes from 1-3 digits") {
        val countryCodes =
          List(("7", "6000000000"), ("54", "111512345678"), ("374", "22290000"))
        for ((country, subscriber) <- countryCodes) {
          val number = new InternationalPhoneNumber(country, subscriber)
          assert(
            (
              number.countryCode,
              number.subscriberNumber
            ) == (country, subscriber)
          )
        }
      }

      it("disallows any country code > 3 digits") {
        assertThrows[IllegalArgumentException] {
          new InternationalPhoneNumber("9999", "99999999")
        }
      }
      it("requires the total number length be 15 or fewer characters") {
        val (country, subscriber) = ("54", "1115123456789")
        val number = new InternationalPhoneNumber(country, subscriber)
        assert(
          (number.countryCode, number.subscriberNumber) == (country, subscriber)
        )
        assertThrows[IllegalArgumentException] {
          new InternationalPhoneNumber(country, s"${subscriber}9")
        }
      }
      it("requires the phone number to be entirely digits") {
        val (country, subscriber) = ("5O", "III5123456789")
        assertThrows[IllegalArgumentException] {
          new InternationalPhoneNumber(country, subscriber)
        }
      }
    }
    describe(".toString") {
      it("Adds the + to the string representation of itself") {
        val (country, subscriber) = ("54", "1115123456789")
        val number = new InternationalPhoneNumber(country, subscriber)
        assert(number.toString() == s"+${country}${subscriber}")
      }
    }
  }

  describe("UsPhoneNumber") {
    describe("constructor") {
      it(
        "requires 3-digit area codes, 3-digit office codes, and 4 digit subscriber numbers"
      ) {
        val validNumberArguments = ("555", "555", "0100")
        val invalidPhoneNumberArguments = List(
          ("5", "555", "0010"),
          ("555", "5555", "0010"),
          ("555", "555", "00100"),
          ("555", "55", "0100"),
          ("555", "555", "100")
        )
        val validNumber = (UsPhoneNumber.apply _).tupled(validNumberArguments)
        assert(
          (
            validNumber.areaCode,
            validNumber.officeCode,
            validNumber.lineNumber
          ) == validNumberArguments
        )
        invalidPhoneNumberArguments.foreach(args => {
          assertThrows[IllegalArgumentException] {
            (UsPhoneNumber.apply _).tupled(args)
          }
        })
      }
      it("Assembles an E.164 subscriber number out of the smaller units") {
        val (area, exchange, serial) = ("555", "555", "0100")
        val validNumber = new UsPhoneNumber(area, exchange, serial)
        assert(validNumber.subscriberNumber == s"${area}${exchange}${serial}")
      }
      it("disallows invalid area and office codes") {
        // Per the wikipedia:
        // Using 0 or 1 as the first digit of an area code or central office code is invalid, as is a 9 as the middle
        // digit of an area code; these are trunk prefixes or reserved for North American Numbering Plan expansion.
        val invalidPhoneNumberArguments = List(
          ("055", "555", "0100"),
          ("155", "555", " 0500"),
          ("555", "055", "0100"),
          ("555", "155", " 0500"),
          ("595", "555", "0100")
        )
        invalidPhoneNumberArguments.foreach(args => {
          assertThrows[IllegalArgumentException] {
            (UsPhoneNumber.apply _).tupled(args)
          }
        })

      }
    }
    describe(".toString") {
      it("adds the + and US country code to its string representation") {
        val (area, exchange, serial) = ("555", "555", "0100")
        val validNumber = new UsPhoneNumber(area, exchange, serial)
        assert(validNumber.toString() == s"+1${area}${exchange}${serial}")
      }
    }
  }
  describe("PhoneNumber") {
    describe("$apply") {
      it("returns a UsPhoneNumber for numbers in the +1 country calling code") {
        val number = PhoneNumber("+15555550100")
        assert(number.isInstanceOf[UsPhoneNumber])
      }
      it("returns an international number for non-US numbers") {
        val number = PhoneNumber("+54111512345678")
        assert(number.isInstanceOf[InternationalPhoneNumber])
      }
      it("requires a E.164-formatted string to return anything at all") {
        assertThrows[IllegalArgumentException] {
          PhoneNumber("5555550100")
        }

      }
    }
  }

  describe("E164NumberFailureReason") {
    describe(".toUserFriendlyReason") {
      for (detailedFailureReason <- E164NumberFailureReason.values) {
        it(
          s"converts $detailedFailureReason into a valid UserFriendlyFailureReason"
        ) {
          val userFriendlyReason = detailedFailureReason.toUserFriendlyReason()
          assert(
            userFriendlyReason
              .isInstanceOf[UserFriendlyPhoneNumberFailureReason]
          )
        }
      }
    }
  }
