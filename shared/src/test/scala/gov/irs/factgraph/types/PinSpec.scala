package gov.irs.factgraph.types

import org.scalatest.funspec.AnyFunSpec

class PinSpec extends AnyFunSpec:
  describe("Pin") {
    describe("preconditions for the pin fields") {
      it("throws an error because pin is the wrong length") {
        try {
          new Pin("123")
        } catch {
          case e: PinValidationFailure => {
            assert(e.validationMessage.toString() == "InvalidPin")
          }
        }
      }
      it("throws an error because pin has invalid characters") {
        try {
          new Pin("abcde")
        } catch {
          case e: PinValidationFailure => {
            assert(e.validationMessage.toString() == "InvalidPin")
          }
        }
      }
      it("throws an error because pin has all zeroes") {
        try {
          new Pin("00000")
        } catch {
          case e: PinValidationFailure => {
            assert(e.validationMessage.toString() == "InvalidAllZerosPin")
          }
        }
      }
    }
    describe("with a 5 digit pin 0-9") {
      it("succeeds") {
        new Pin("01234")
      }
    }
  }
