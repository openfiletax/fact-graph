package gov.irs.factgraph.types

import org.scalatest.funspec.AnyFunSpec

class RationalSpec extends AnyFunSpec:
  describe("Rational") {
    describe(".+") {
      it("adds the values") {
        assert(Rational("1/2") + Rational("1/3") == Rational("5/6"))
      }

      it("preserves equivalent denominators") {
        assert(Rational("12/100") + Rational("38/100") == Rational("50/100"))
      }
    }

    describe(".-") {
      it("subtracts the values") {
        assert(Rational("1/2") - Rational("1/3") == Rational("1/6"))
      }

      it("preserves equivalent denominators") {
        assert(Rational("43/100") - Rational("21/100") == Rational("22/100"))
      }
    }

    describe(".*") {
      it("multiplies the values") {
        assert(Rational("1/2") * Rational("1/3") == Rational("1/6"))
      }
    }

    describe("./") {
      it("divides the values") {
        assert(Rational("1/2") / Rational("1/3") == Rational("3/2"))
      }

      describe("when the denominator is zero") {
        it("throws an exception") {
          assertThrows[ArithmeticException] {
            Rational("2/3") / Rational(0)
          }
        }
      }
    }

    describe(".<") {
      it("returns true if the left hand side is less than the right") {
        assert(Rational("2/3") < Rational("3/4"))
        assert(!(Rational("2/3") < Rational("2/3")))
        assert(!(Rational("2/3") < Rational("1/2")))
      }
    }

    describe(".>") {
      it("returns true if the left hand side is greater than the right") {
        assert(!(Rational("2/3") > Rational("3/4")))
        assert(!(Rational("2/3") > Rational("2/3")))
        assert(Rational("2/3") > Rational("1/2"))
      }
    }

    describe(".<=") {
      it(
        "returns true if the left hand side is less than or equal to the right"
      ) {
        assert(Rational("2/3") <= Rational("3/4"))
        assert(Rational("2/3") <= Rational("2/3"))
        assert(!(Rational("2/3") <= Rational("1/2")))
      }
    }

    describe(".>=") {
      it(
        "returns true if the left hand side is greater than or equal to the right"
      ) {
        assert(!(Rational("2/3") >= Rational("3/4")))
        assert(Rational("2/3") >= Rational("2/3"))
        assert(Rational("2/3") >= Rational("1/2"))
      }
    }

    describe(".numerator") {
      it("returns the numerator") {
        assert(Rational("2/3").numerator == 2)
      }
    }

    describe(".denominator") {
      it("returns the denominator") {
        assert(Rational("2/3").denominator == 3)
      }
    }

    describe(".reciprocal") {
      it("finds the reciprocal") {
        assert(Rational("2/3").reciprocal == Rational("3/2"))
      }

      describe("when the numerator is zero") {
        it("throws an exception to prevent a zero denominator") {
          assertThrows[ArithmeticException] {
            Rational(0).reciprocal
          }
        }
      }
    }

    describe(".simplify") {
      it("simplifies the rational") {
        assert(Rational("9/81").simplify == Rational("1/9"))
      }

      it("always returns a positive denominator") {
        assert(Rational("-9/81").simplify == Rational("-1/9"))
        assert(Rational("9/-81").simplify == Rational("-1/9"))
        assert(Rational("-9/-81").simplify == Rational("1/9"))
      }
    }

    describe(".toString") {
      it("expresses the rational as a fraction") {
        assert(Rational(2, 3).toString == "2/3")
      }
    }

    describe("$apply") {
      it("rejects zero denominators") {
        assertThrows[IllegalArgumentException] {
          Rational(1, 0)
        }

        assertThrows[IllegalArgumentException] {
          Rational("1/0")
        }
      }

      it("rejects invalid strings") {
        assertThrows[NumberFormatException] {
          Rational("get/it")
        }
      }
    }
  }

  describe("Numeric[Rational]") {
    describe(".fromInt") {
      it("returns a rational with 1 as the denominator") {
        assert(Numeric[Rational].fromInt(3) == Rational("3/1"))
      }
    }

    describe(".negate") {
      it("negates positive rationals") {
        assert(Numeric[Rational].negate(Rational("2/3")) == Rational("-2/3"))
      }

      it("negates negative rationals") {
        assert(Numeric[Rational].negate(Rational("-2/3")) == Rational("2/3"))
      }
    }

    describe(".parseString") {
      it("parses valid strings") {
        assert(Numeric[Rational].parseString("2/3").contains(Rational("2/3")))
      }

      it("returns None if the string is not valid") {
        assert(Numeric[Rational].parseString("yo").isEmpty)
      }
    }

    describe(".toDouble") {
      it("throws an exception") {
        assertThrows[UnsupportedOperationException] {
          Numeric[Rational].toDouble(Rational("2/3"))
        }
      }
    }

    describe(".toFloat") {
      it("throws an exception") {
        assertThrows[UnsupportedOperationException] {
          Numeric[Rational].toFloat(Rational("2/3"))
        }
      }
    }

    describe(".toInt") {
      it("throws an exception") {
        assertThrows[UnsupportedOperationException] {
          Numeric[Rational].toInt(Rational("2/3"))
        }
      }
    }

    describe(".toLong") {
      it("throws an exception") {
        assertThrows[UnsupportedOperationException] {
          Numeric[Rational].toLong(Rational("2/3"))
        }
      }
    }
  }
