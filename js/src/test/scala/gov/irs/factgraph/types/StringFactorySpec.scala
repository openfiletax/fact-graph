package gov.irs.factgraph.types

import org.scalatest.funspec.AnyFunSpec
import scala.collection.Factory.StringFactory

class StringFactorySpec extends AnyFunSpec:

  describe("String Factory with no Match limit") {
    it("one word") {
      assert(StringFactory.apply("Firstname").right === "Firstname")
    }
    it("multiple words") {
      assert(StringFactory.apply("Company Name").right === "Company Name")
    }
  }

  describe("String Factory with DefaultNamePattern Match limit") {
    it("one word with no invalid characters") {
      assert(
        StringFactory
          .apply("Firstname", StringFactory.DefaultNamePattern)
          .right === "Firstname"
      )
    }
    it("multiple words with no invalid characters") {
      assert(
        StringFactory
          .apply("Company Name", StringFactory.DefaultNamePattern)
          .right === "Company Name"
      )
    }
    it("one word with invalid characters") {
      val errorReason = StringFactory
        .apply("Firstname$", StringFactory.DefaultNamePattern)
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString()
      assert(errorReason === "InvalidCharacters")
    }
    it("multiple words with invalid characters") {
      val errorReason = StringFactory
        .apply("Firstname$ Lastname!", StringFactory.DefaultNamePattern)
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString()
      assert(errorReason === "InvalidCharacters")
    }
  }

  describe("String Factory with EmployerNameLine1Pattern Match limit") {
    it("one word with no invalid characters") {
      assert(
        StringFactory
          .apply("Firstname", StringFactory.EmployerNameLine1Pattern)
          .right === "Firstname"
      )
    }
    it("multiple words with no invalid characters - test &") {
      assert(
        StringFactory
          .apply("Company & Name", StringFactory.EmployerNameLine1Pattern)
          .right === "Company & Name"
      )
    }
    it("multiple words with no invalid characters - test '") {
      assert(
        StringFactory
          .apply("Matt's & Son", StringFactory.EmployerNameLine1Pattern)
          .right === "Matt's & Son"
      )
    }
    it("multiple words with no invalid characters - test /, ', and & ") {
      assert(
        StringFactory
          .apply("Matt's & Son", StringFactory.EmployerNameLine1Pattern)
          .right === "Matt's & Son"
      )
    }
    it("one word with invalid characters") {
      val errorReason = StringFactory
        .apply("Son$", StringFactory.EmployerNameLine1Pattern)
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString()
      assert(errorReason === "InvalidEmployerNameLine1Characters")
    }
    it("multiple words with invalid characters, !") {
      val errorReason = StringFactory
        .apply("c/o Matt's & Son!", StringFactory.EmployerNameLine1Pattern)
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString()
      assert(errorReason === "InvalidEmployerNameLine1Characters")
    }
    it("multiple words with invalid characters, $") {
      val errorReason = StringFactory
        .apply("c/o Matt's $ Son", StringFactory.EmployerNameLine1Pattern)
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString()
      assert(errorReason === "InvalidEmployerNameLine1Characters")
    }
    it("multiple words with two spaces characters") {
      val errorReason = StringFactory
        .apply("Firstname  Lastname", StringFactory.EmployerNameLine1Pattern)
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString()
      assert(errorReason === "InvalidEmployerNameLine1Characters")
    }
  }

  describe("String Factory with EmployerNameLine2Pattern Match limit") {
    it("one word with no invalid characters") {
      assert(
        StringFactory
          .apply("Firstname", StringFactory.EmployerNameLine2Pattern)
          .right === "Firstname"
      )
    }
    it("multiple words with no invalid characters - test &") {
      assert(
        StringFactory
          .apply("Company & Name", StringFactory.EmployerNameLine2Pattern)
          .right === "Company & Name"
      )
    }
    it("multiple words with no invalid characters - test '") {
      assert(
        StringFactory
          .apply("Matt's & Son", StringFactory.EmployerNameLine2Pattern)
          .right === "Matt's & Son"
      )
    }
    it("multiple words with no invalid characters - test /, ', and & ") {
      assert(
        StringFactory
          .apply("c/o Matt's & Son", StringFactory.EmployerNameLine2Pattern)
          .right === "c/o Matt's & Son"
      )
    }
    it("multiple words with no invalid characters - test /, ', %, and & ") {
      assert(
        StringFactory
          .apply(
            "c/o Matt's & Son % Junior",
            StringFactory.EmployerNameLine2Pattern
          )
          .right === "c/o Matt's & Son % Junior"
      )
    }
    it("one word with invalid characters") {
      val errorReason = StringFactory
        .apply("Son$", StringFactory.EmployerNameLine2Pattern)
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString()
      assert(errorReason === "InvalidEmployerNameLine2Characters")
    }
    it("multiple words with invalid characters, !") {
      val errorReason = StringFactory
        .apply("c/o Matt's & Son!", StringFactory.EmployerNameLine2Pattern)
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString()
      assert(errorReason === "InvalidEmployerNameLine2Characters")
    }
    it("multiple words with invalid characters, $") {
      val errorReason = StringFactory
        .apply("c/o Matt's $ Son", StringFactory.EmployerNameLine2Pattern)
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString()
      assert(errorReason === "InvalidEmployerNameLine2Characters")
    }
    it("multiple words with two spaces characters") {
      val errorReason = StringFactory
        .apply("Firstname  Lastname", StringFactory.EmployerNameLine2Pattern)
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString()
      assert(errorReason === "InvalidEmployerNameLine2Characters")
    }
  }

  describe("String Factory with Match limit not defined") {
    it("one word with no invalid characters") {
      assert(
        StringFactory
          .apply("Firstname", "[\\sA-Za-z0-9]+")
          .right === "Firstname"
      )
    }
    it("multiple words with no invalid characters") {
      assert(
        StringFactory
          .apply("Company Name", "[\\sA-Za-z0-9]+")
          .right === "Company Name"
      )
    }
    it("one word with invalid characters") {
      val errorReason = StringFactory
        .apply("First%&'name$", "[\\sA-Za-z0-9]+")
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString()
      assert(errorReason === "InvalidCharacters")
    }
    it("multiple words with invalid characters") {
      val errorReason = StringFactory
        .apply("Firstname$ Lastname!", "[\\sA-Za-z0-9]+")
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString()
      assert(errorReason === "InvalidCharacters")
    }
  }

  describe("String Factory with Numbers only Match Limit") {
    it("one word with no invalid characters") {
      assert(
        StringFactory
          .apply("123", StringFactory.NumbersOnlyPattern)
          .right === "123"
      )
    }
    it("one word with invalid characters") {
      val errorReason = StringFactory
        .apply("123F", StringFactory.NumbersOnlyPattern)
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString()
      assert(errorReason === "InvalidCharactersNumbersOnly")
    }
    it("multiple words with invalid characters") {
      val errorReason = StringFactory
        .apply("123 4", StringFactory.NumbersOnlyPattern)
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString()
      assert(errorReason === "InvalidCharactersNumbersOnly")
    }
  }

  describe("String Factory with Invalid Form 1099R Year Match Limit") {
    it("one word with no invalid characters") {
      assert(
        StringFactory
          .apply("1234", StringFactory.Form1099rBox11YearPattern)
          .right === "1234"
      )
    }
    it("one word with invalid characters") {
      val errorReason = StringFactory
        .apply("123F", StringFactory.Form1099rBox11YearPattern)
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString()
      assert(errorReason === "InvalidForm1099rBox11Year")
    }
    it("multiple words with invalid characters") {
      val errorReason = StringFactory
        .apply("1234 F", StringFactory.Form1099rBox11YearPattern)
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString()
      assert(errorReason === "InvalidForm1099rBox11Year")
    }
  }

  describe("String Factory with StripChars") {
    it("returns a string without an invalid character") {
      val stripCharsResult = StringFactory
        .stripDisallowedCharacters("test$", "A-Za-z")
      assert(
        stripCharsResult === "test"
      )
    }
    it("returns the same string if it doesn't have invalid characters") {
      val stripCharsResult = StringFactory
        .stripDisallowedCharacters("test", "A-Za-z")
      assert(
        stripCharsResult === "test"
      )
    }
    it("empty string returns an empty string") {
      val stripCharsResult = StringFactory
        .stripDisallowedCharacters("", "A-Za-z")
      assert(
        stripCharsResult === ""
      )
    }
  }
