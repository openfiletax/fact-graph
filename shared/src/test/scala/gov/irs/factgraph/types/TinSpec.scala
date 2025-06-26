package gov.irs.factgraph.types

import org.scalatest.funspec.AnyFunSpec

class TinSpec extends AnyFunSpec:

  def assertThrowsWithReason(op: => Any, reason: TinFailureReason) =
    val failure = intercept[TinValidationFailure](op)
    assert(failure.validationMessage === reason)

  describe("Tin") {
    describe("$apply") {
      it("requires nine digits to construct") {
        val range = Range(1, 10)
        for (len <- range) do
          val tinStr: String = "111111111".slice(0, len)
          if (len != 9) {
            assertThrows[TinValidationFailure] {
              Tin(tinStr)
            }
          } else {
            assert(Tin(tinStr).isInstanceOf[Tin])
          }

      }
      it("can handle punctuation in the input string") {
        val (area, group, serial) = ("999", "00", "0000")
        List("-", ".", ",", " ", "/", "\\", "").foreach(punc =>
          val tinStr = s"${area}${punc}${group}${punc}${serial}"
          val tin = Tin(tinStr)
          assert((tin.area, tin.group, tin.serial) == (area, group, serial))
        )
      }
      it("Never strips leading 0's") {
        val tin = Tin("001-01-0000")
        assert((tin.area, tin.group, tin.serial) == ("001", "01", "0000"))
      }
    }
    describe(".toString") {
      it("Adds hyphens between the groupings of digits") {
        assert(Tin("999", "00", "0000", false).toString == "999-00-0000")
      }
    }
    describe("equals") {
      it("Considers two TINs that have the same digits to be equal") {
        assert(
          Tin("999", "00", "0000", false) == Tin("999", "00", "0000", false)
        )
      }
    }
    describe("SSN/ITIN") {
      it("can tell an ITIN by whether it follows the Publication 4757 rules") {
        assert(Tin("999-00-0001").isITIN)
        assert(Tin("999-50-0001").isITIN)
        assert(!Tin("999-49-0001").isITIN)
        assert(Tin("999-80-0001").isITIN)
        assert(!Tin("899-80-0001").isITIN)
        assert(Tin("999-90-0001").isITIN)
        assert(Tin("999-92-0001").isITIN)
        assert(Tin("999-95-0001").isITIN)
        assert(!Tin("999-93-0001").isITIN)
        assert(Tin("999-99-0001").isITIN)
      }
    }
    describe("ATIN") {
      it(
        "can tell an ATIN by whether it starts with a 9 and has a group of 93"
      ) {
        assert(Tin("999-93-0001").isATIN)
        assert(!Tin("899-93-0001").isATIN)
        assert(!Tin("999-92-0001").isATIN)
      }
    }
    it(
      "can tell if an SSN starts with an invalid area number or ends with an invalid serial"
    ) {
      assert(!Tin("999-50-0001").isSSN)
      assert(Tin("100-50-0001").isSSN)
      assert(!Tin("100-20-0000").isSSN)
      // 000-00-0000 is a special case that we treat as an SSN
      assert(Tin("000-00-0000", true).isSSN)
    }

  }

  describe("TinFailureReason") {
    describe(".toUserFriendlyReason") {
      for (detailedFailureReason <- TinFailureReason.values) {
        it(
          s"converts $detailedFailureReason into a valid UserFriendlyTinFailureReason"
        ) {
          val userFriendlyReason = detailedFailureReason.toUserFriendlyReason()
          assert(userFriendlyReason.isInstanceOf[UserFriendlyTinFailureReason])
        }
      }
    }
  }

  describe("Check for invalid TINs") {
    it("throws an error when if TIN starts with 000") {
      assertThrowsWithReason(
        { Tin("000-55-0001") },
        TinFailureReason.InvalidTin
      )
    }

    it("throws an error when if TIN starts with 666") {
      assertThrowsWithReason(
        { Tin("666-55-0001") },
        TinFailureReason.InvalidTin
      )
    }
    it("throws an error when if TIN is 000-00-0000 by default") {
      assertThrowsWithReason(
        { Tin("000-00-0000") },
        TinFailureReason.InvalidTin
      )
    }
    it("allows a TIN is 000-00-0000 with a setting") {
      assert(Tin("000-00-0000", allowAllZeros = true).isInstanceOf[Tin])
    }

  }
