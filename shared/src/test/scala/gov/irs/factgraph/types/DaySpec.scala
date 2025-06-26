package gov.irs.factgraph.types

import org.scalatest.funspec.AnyFunSpec

class DaySpec extends AnyFunSpec {
  describe("Day") {
    describe(".>") {
      it("returns true if the left hand side is greater than the right") {
        assert(!(Day("2020-01-01") > Day("2022-02-02")))
        assert(!(Day("2023-02-02") > Day("2023-02-02")))
        assert(Day("2023-03-03") > Day("2020-01-01"))
      }
    }
    describe(".>=") {
      it(
        "returns true if the left hand side is greater than or equal to the right"
      ) {
        assert(!(Day("2020-01-01") >= Day("2022-02-02")))
        assert(Day("2023-02-02") >= Day("2023-02-02"))
        assert(Day("2023-03-03") >= Day("2020-01-01"))
      }
    }
    describe(".==") {
      it("returns true if the left hand side is equal to the right") {
        assert(!(Day("2020-01-01") == Day("2022-02-02")))
        assert(Day("2023-02-02") == Day("2023-02-02"))
        assert(!(Day("2023-03-03") == Day("2020-01-01")))
      }
    }
    describe(".<=") {
      it("returns true if the left hand side is equal to the right") {
        assert(Day("2020-01-01") <= Day("2022-02-02"))
        assert(Day("2023-02-02") <= Day("2023-02-02"))
        assert(!(Day("2023-03-03") <= Day("2020-01-01")))
      }
    }
    describe(".<") {
      it("returns true if the left hand side is equal to the right") {
        assert(Day("2020-01-01") < Day("2022-02-02"))
        assert(!(Day("2023-02-02") < Day("2023-02-02")))
        assert(!(Day("2023-03-03") < Day("2020-01-01")))
      }
    }
  }
  describe("Day validation") {
    it("should fail the month because it's not between 1-12") {
      try {
        Day("2000-13-22")
      } catch {
        case e: java.time.format.DateTimeParseException => {
          assert(e.getMessage().contains("MonthOfYear"))
        }
      }
    }
    it("should fail the day because no month can not have 32 days") {
      try {
        Day("2000-09-32")
      } catch {
        case e: java.time.format.DateTimeParseException => {
          assert(e.getMessage().contains("DayOfMonth"))
        }
      }
    }
    it("should fail the day because september only has 30 days") {
      try {
        Day("2000-09-31")
      } catch {
        case e: java.time.format.DateTimeParseException => {
          assert(e.getMessage().contains("Invalid date"))
        }
      }
    }
    it("should fail the due to leap year") {
      try {
        Day("2001-02-29")
      } catch {
        case e: java.time.format.DateTimeParseException => {
          assert(e.getMessage().contains("leap year"))
        }
      }
    }
  }
}
