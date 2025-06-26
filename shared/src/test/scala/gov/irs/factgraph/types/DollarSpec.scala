package gov.irs.factgraph.types

import org.scalatest.funspec.AnyFunSpec

class DollarSpec extends AnyFunSpec:
  /** Helper method to simplify checking that a DollarFailureReason is thrown
    * with the correct reason.
    *
    * @param reason
    * @param op
    */
  def assertThrowsWithReason(op: => Any, reason: DollarFailureReason) =
    val failure = intercept[DollarValidationFailure](op)
    assert(failure.validationMessage === reason)

  describe("Dollar") {
    describe("$apply") {
      it("parses whole numbers without decimal values correctly") {
        assert(Dollar("1") === 1)
      }

      it("parses whole numbers with a decimal and no cents") {
        assert(Dollar("1.") === 1)
      }

      it("parses whole numbers with decimals values correctly") {
        assert(Dollar("1.0") === 1)
        assert(Dollar("1.00") === 1)
      }

      it("parses fractional values correctly") {
        assert(Dollar("1.23") === 1.23)
      }

      it("parses hyphenated values as negative") {
        assert(Dollar("-1") === -1)
        assert(Dollar("-1.23") === -1.23)
      }

      it("parses parenthetical values as negative") {
        assert(Dollar("(1)") === -1)
        assert(Dollar("(1.23)") === -1.23)
      }

      it("allows any combination of parentheses") {
        assert(Dollar(")1)") === -1)
        assert(Dollar(")1.23)") === -1.23)

        assert(Dollar("(1(") === -1)
        assert(Dollar("(1.23(") === -1.23)

        assert(Dollar(")1(") === -1)
        assert(Dollar(")1.23(") === -1.23)
      }

      it("allows commas in the whole numbers portion") {
        assert(Dollar("1,234.00") === 1234)
        assert(Dollar("1,234") === 1234)
        assert(Dollar("12,34") === 1234)
        assert(Dollar("1234,") === 1234)
        assert(Dollar("1,2,3,4,") === 1234)
        assert(Dollar("12,,34,,") === 1234)
      }

      it("rejects disallowed characters") {
        assertThrowsWithReason(
          { Dollar("1.0a") },
          DollarFailureReason.InvalidCharacters
        )
        assertThrowsWithReason(
          { Dollar("abc") },
          DollarFailureReason.InvalidCharacters
        )
      }

      it("rejects commas in the fractional portion") {
        assertThrowsWithReason(
          { Dollar("1.2,3") },
          DollarFailureReason.InvalidCharacters
        )
        assertThrowsWithReason(
          { Dollar("1.23,") },
          DollarFailureReason.InvalidCharacters
        )
      }

      it("rejects values with more than one decimal") {
        assertThrowsWithReason(
          { Dollar("1.2.3") },
          DollarFailureReason.TooManyDecimals
        )
      }

      it("rejects values with a hyphen anywhere except the beginning") {
        assertThrowsWithReason(
          { Dollar("1.-3") },
          DollarFailureReason.InvalidHyphens
        )
        assertThrowsWithReason(
          { Dollar("1-.3") },
          DollarFailureReason.InvalidHyphens
        )
        assertThrowsWithReason(
          { Dollar("1.3-") },
          DollarFailureReason.InvalidHyphens
        )
      }

      it(
        "rejects values composed of just a pair of parentheses with InvalidParentheses"
      ) {
        assertThrowsWithReason(
          { Dollar("()") },
          DollarFailureReason.InvalidParentheses
        )
        assertThrowsWithReason(
          { Dollar("((") },
          DollarFailureReason.InvalidParentheses
        )
        assertThrowsWithReason(
          { Dollar("))") },
          DollarFailureReason.InvalidParentheses
        )
        assertThrowsWithReason(
          { Dollar(")(") },
          DollarFailureReason.InvalidParentheses
        )
      }

      it("rejects values composed of a single hyphen with InvalidHyphens") {
        assertThrowsWithReason(
          { Dollar("-") },
          DollarFailureReason.InvalidHyphens
        )
      }

      describe("when rounding is not disabled") {
        it("accepts values more than two fractional digits") {
          // NOTE: Rounding logic is tested in direct-file/df-client/df-client-app/src/test/taxCalculation.test.ts
          Dollar("1.234")
        }
      }
      describe("when rounding is disabled") {
        it("rejects values with more than two fractional digits") {
          assertThrowsWithReason(
            { Dollar("1.234", allowRounding = false) },
            DollarFailureReason.TooManyFractionalDigits
          )
        }
      }

      it("rejects values that contain both hyphens and parentheses") {
        assertThrowsWithReason(
          { Dollar("(-1)") },
          DollarFailureReason.CombinedHyphensAndParentheses
        )

        assertThrowsWithReason(
          { Dollar("(-1.23)") },
          DollarFailureReason.CombinedHyphensAndParentheses
        )
      }

      it("rejects values with unmatched parentheses") {
        // Correct position, but alone
        assertThrowsWithReason(
          { Dollar("(1.0") },
          DollarFailureReason.InvalidParentheses
        )
        assertThrowsWithReason(
          { Dollar("1.0(") },
          DollarFailureReason.InvalidParentheses
        )
        assertThrowsWithReason(
          { Dollar(")1.0") },
          DollarFailureReason.InvalidParentheses
        )
        assertThrowsWithReason(
          { Dollar("1.0)") },
          DollarFailureReason.InvalidParentheses
        )
      }

      it("rejects values with parentheses anywhere except the ends") {
        // Single parentheses
        assertThrowsWithReason(
          { Dollar("1).0") },
          DollarFailureReason.InvalidParentheses
        )
        assertThrowsWithReason(
          { Dollar("1(.0") },
          DollarFailureReason.InvalidParentheses
        )
        assertThrowsWithReason(
          { Dollar("1.)0") },
          DollarFailureReason.InvalidParentheses
        )
        assertThrowsWithReason(
          { Dollar("1.(0") },
          DollarFailureReason.InvalidParentheses
        )

        // Matching pair, but one is in the wrong position
        assertThrowsWithReason(
          { Dollar("(1).0") },
          DollarFailureReason.InvalidParentheses
        )
        assertThrowsWithReason(
          { Dollar("(1.)0") },
          DollarFailureReason.InvalidParentheses
        )
        assertThrowsWithReason(
          { Dollar("1.(0)") },
          DollarFailureReason.InvalidParentheses
        )
        assertThrowsWithReason(
          { Dollar("1(.0)") },
          DollarFailureReason.InvalidParentheses
        )

        // Matching pair, neither in the correct position
        assertThrowsWithReason(
          { Dollar("1(2).3") },
          DollarFailureReason.InvalidParentheses
        )
        assertThrowsWithReason(
          { Dollar("1(2.)3") },
          DollarFailureReason.InvalidParentheses
        )
        assertThrowsWithReason(
          { Dollar("1.(2)3") },
          DollarFailureReason.InvalidParentheses
        )
        assertThrowsWithReason(
          { Dollar("1(.2)3") },
          DollarFailureReason.InvalidParentheses
        )
      }
    }

    describe(".+") {
      it("adds the values") {
        val plus = Dollar("1.23") + Dollar("2.34")
        assert(plus == Dollar("3.57"))
        assert(plus.scale == 2)
      }
    }

    describe(".-") {
      it("subtracts the values") {
        val minus = Dollar("1.23") - Dollar("2.34")
        assert(minus == Dollar("-1.11"))
        assert(minus.scale == 2)
      }
    }

    describe(".*") {
      it("multiplies the values") {
        val times = Dollar("1.23") * Dollar("2.34")
        assert(times == Dollar("2.88"))
        assert(times.scale == 2)
      }
    }

    describe("./") {
      it("divides the values") {
        val div = Dollar("1.23") / Dollar("2.34")
        assert(div == Dollar("0.53"))
        assert(div.scale == 2)
      }
    }

    describe(".<") {
      it("returns true if the left hand side is less than the right") {
        assert(Dollar("1.23") < Dollar("2.34"))
        assert(!(Dollar("1.23") < Dollar("1.23")))
        assert(!(Dollar("1.23") < Dollar("0.98")))
      }
    }

    describe(".>") {
      it("returns true if the left hand side is greater than the right") {
        assert(!(Dollar("1.23") > Dollar("2.34")))
        assert(!(Dollar("1.23") > Dollar("1.23")))
        assert(Dollar("1.23") > Dollar("0.98"))
      }
    }

    describe(".<=") {
      it(
        "returns true if the left hand side is less than or equal to the right"
      ) {
        assert(Dollar("1.23") <= Dollar("2.34"))
        assert(Dollar("1.23") <= Dollar("1.23"))
        assert(!(Dollar("1.23") <= Dollar("0.98")))
      }
    }

    describe(".>=") {
      it(
        "returns true if the left hand side is greater than or equal to the right"
      ) {
        assert(!(Dollar("1.23") >= Dollar("2.34")))
        assert(Dollar("1.23") >= Dollar("1.23"))
        assert(Dollar("1.23") >= Dollar("0.98"))
      }
    }

    describe(".intValue") {
      it("returns the whole dollar value") {
        assert(Dollar("3.01").intValue == 3)
        assert(Dollar("3.99").intValue == 3)
      }
    }

    describe(".round") {
      describe("when less than halfway between whole dollar values") {
        it("rounds down") {
          val roundDown = Dollar("3.49").round
          assert(roundDown == Dollar("3.00"))
          assert(roundDown.scale == 2)
        }
      }

      describe("when halfway between whole dollar values") {
        it("rounds up") {
          val roundUp = Dollar("3.50").round
          assert(roundUp == Dollar("4.00"))
          assert(roundUp.scale == 2)
        }
      }
    }

    describe("when provided a value with fractional cents") {
      it("uses banker's rounding") {
        assert(Dollar(BigDecimal(1.005)) == Dollar("1.00"))
        assert(Dollar(BigDecimal(1.015)) == Dollar("1.02"))
      }
    }
  }

  describe("Numeric[Dollar]") {
    describe(".fromInt") {
      it("translates the integer into the whole dollar value") {
        assert(Numeric[Dollar].fromInt(123) == Dollar("123.00"))
      }
    }

    describe(".negate") {
      it("negates positive dollar values") {
        assert(Numeric[Dollar].negate(Dollar("1.23")) == Dollar("-1.23"))
      }

      it("negates negative dollar values") {
        assert(Numeric[Dollar].negate(Dollar("-1.23")) == Dollar("1.23"))
      }
    }

    describe(".parseString") {
      it("parses valid strings") {
        assert(Numeric[Dollar].parseString("1.01").contains(Dollar("1.01")))
      }

      it("returns None if the string is not valid") {
        assert(Numeric[Dollar].parseString("yo").isEmpty)
      }
    }

    describe(".toDouble") {
      it("throws an exception") {
        assertThrows[UnsupportedOperationException] {
          Numeric[Dollar].toDouble(Dollar("1.23"))
        }
      }
    }

    describe(".toFloat") {
      it("throws an exception") {
        assertThrows[UnsupportedOperationException] {
          Numeric[Dollar].toFloat(Dollar("1.23"))
        }
      }
    }

    describe(".toInt") {
      it("throws an exception") {
        assertThrows[UnsupportedOperationException] {
          Numeric[Dollar].toInt(Dollar("1.23"))
        }
      }
    }

    describe(".toLong") {
      it("throws an exception") {
        assertThrows[UnsupportedOperationException] {
          Numeric[Dollar].toLong(Dollar("1.23"))
        }
      }
    }
  }
