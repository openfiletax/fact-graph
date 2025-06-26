package gov.irs.factgraph.types

import org.scalatest.funspec.AnyFunSpec

class EmailAddressSpec extends AnyFunSpec:
  describe("EmailAddress") {
    describe("$apply") {
      it("it requires an @ in an email address") {
        assert(
          EmailAddress("example@example.com")
            .toString() == "example@example.com"
        )
        assertThrows[IllegalArgumentException] {
          EmailAddress("example&example.com")
        }
      }
    }
  }
