package gov.irs.factgraph.types

import org.scalatest.funspec.AnyFunSpec

class EinSpec extends AnyFunSpec:
  describe("Ein") {
    describe("$apply") {
      it("can handle punctuation in the input string") {
        val (prefix, serial) = ("31", "1234567")
        List("-", ".", ",", " ", "/", "\\", "").foreach(punc =>
          val einStr = s"${prefix}${punc}${serial}"
          val ein = Ein(einStr)
          assert((ein.prefix, ein.serial) == (prefix, serial))
        )
      }
      it("Never strips leading 0's") {
        val ein = Ein("06-1234567")
        assert((ein.prefix, ein.serial) == ("06", "1234567"))
      }
      it("throws an error with an invalid prefix") {
        assertThrows[EinValidationFailure] {
          Ein("19-1234567")
        }
      }
    }
    describe(".toString") {
      it("Adds hyphens between the groupings of digits") {
        assert(Ein("31", "1234567").toString == "31-1234567")
      }
    }
    describe("equals") {
      it("Considers two TINs that have the same digits to be equal") {
        assert(Ein("31", "1234567") == Ein("31", "1234567"))
      }
    }

  }

  describe("EinFailureReason") {
    describe(".toUserFriendlyReason") {
      for (detailedFailureReason <- EinFailureReason.values) {
        it(
          s"converts $detailedFailureReason into a valid UserFriendlyEinFailureReason"
        ) {
          val userFriendlyReason = detailedFailureReason.toUserFriendlyReason()
          assert(userFriendlyReason.isInstanceOf[UserFriendlyEinFailureReason])
        }
      }
    }
  }
