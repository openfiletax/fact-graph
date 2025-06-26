package gov.irs.factgraph.types

import org.scalatest.funspec.AnyFunSpec

class BankAccountSpec extends AnyFunSpec:
  def buildBankAccount(
      accountType: String = "Checking",
      routingNumber: String = "323075880", // DC Credit Union
      accountNumber: String = "12345ABCDEF"
  ) =
    new BankAccount(accountType, routingNumber, accountNumber)

  describe("BankAccount") {
    it("constructs correctly when fields are valid") {
      val bankAccount = buildBankAccount()

      assert(bankAccount.accountType === "Checking")
      assert(bankAccount.routingNumber === "323075880")
      assert(bankAccount.accountNumber === "12345ABCDEF")
    }

    describe("preconditions for accountType") {
      it("throws an error when accountType is empty") {
        val e = intercept[BankAccountValidationFailure] {
          buildBankAccount(accountType = "")
        }

        assert(
          e.fieldErrors("accountType")
            .validationMessage == BankAccountFailureReason.InvalidAccountType
        )
      }

      it("throws an error when an invalid accountType is provided") {
        val e = intercept[BankAccountValidationFailure] {
          buildBankAccount(accountType = "Crypto")
        }

        assert(
          e.fieldErrors("accountType")
            .validationMessage == BankAccountFailureReason.InvalidAccountType
        )
      }
    }

    describe("preconditions for routingNumber") {
      it("throws an error when routingNumber is empty") {
        val e = intercept[BankAccountValidationFailure] {
          buildBankAccount(routingNumber = "")
        }

        assert(
          e.fieldErrors("routingNumber")
            .validationMessage == BankAccountFailureReason.InvalidRoutingNumber
        )
      }

      describe("when valid routingNumber is provided") {
        val ValidUsRoutingNumbers = List(
          "011000015", // Federal reserve bank
          "323075880", // OnPoint Commmunity Credit Union
          "254074455" // DC Credit Union
        )

        ValidUsRoutingNumbers.foreach { routingNumber =>
          it(s"constructs without error ($routingNumber)") {
            buildBankAccount(routingNumber = routingNumber)
          }
        }
      }

      describe("when routingNumber is invalid") {
        val MalformedUsRoutingNumbers = List(
          // Too short
          "1",
          "11011011",

          // Invalid characters
          "ABCEFGHIJ",
          "011 00 15",
          "><1|5|9#$"
        )

        MalformedUsRoutingNumbers.foreach { routingNumber =>
          it(s"throws a malformed routing number error ($routingNumber)") {
            val e = intercept[BankAccountValidationFailure] {
              buildBankAccount(routingNumber = routingNumber)
            }

            assert(
              e.fieldErrors("routingNumber")
                .validationMessage == BankAccountFailureReason.InvalidRoutingNumber
            )
          }
        }
      }

      describe("when routingNumber's prefix is wrong") {
        val MalformedUsRoutingNumbers = List(
          // doesn't match regex: (01|02|03|04|05|06|07|08|09|10|11|12|21|22|23|24|25|26|27|28|29|30|31|32)[0-9]{7}
          "331234567",
          "131111111",
          "177777777"
        )

        MalformedUsRoutingNumbers.foreach { routingNumber =>
          it(s"throws a malformed routing number error ($routingNumber)") {
            val e = intercept[BankAccountValidationFailure] {
              buildBankAccount(routingNumber = routingNumber)
            }

            assert(
              e.fieldErrors("routingNumber")
                .validationMessage == BankAccountFailureReason.MalformedRoutingNumber
            )
          }
        }
      }

      describe("when routing number fails checksum") {
        val InvalidUsRoutingNumbers = List(
          // Failing checksums
          "110110111",
          "222222222",
          "123456789"
        )

        InvalidUsRoutingNumbers.foreach { routingNumber =>
          it(s"throws a checksum error ($routingNumber)") {
            val e = intercept[BankAccountValidationFailure] {
              buildBankAccount(routingNumber = routingNumber)
            }

            assert(
              e.fieldErrors("routingNumber")
                .validationMessage == BankAccountFailureReason.InvalidRoutingNumberChecksum
            )
          }
        }
      }
    }

    describe("preconditions for accountNumber") {
      describe("when accountNumber is blank") {
        it("throws an MalformedAccountNumber error") {
          val e = intercept[BankAccountValidationFailure] {
            buildBankAccount(accountNumber = "")
          }

          assert(
            e.fieldErrors("accountNumber")
              .validationMessage == BankAccountFailureReason.MalformedAccountNumber
          )
        }
      }

      describe("when a 5 digit account number is provided") {
        it("constructs without error") {
          buildBankAccount(accountNumber = "12345")
        }
      }

      describe("when a 17 digit account number is provided") {
        it("constructs without error") {
          buildBankAccount(accountNumber = "12345678901234567")
        }
      }

      describe("when accountNumber is too short") {
        it("throws an MalformedAccountNumber error") {
          val e = intercept[BankAccountValidationFailure] {
            buildBankAccount(accountNumber = "1234")
          }

          assert(
            e.fieldErrors("accountNumber")
              .validationMessage == BankAccountFailureReason.MalformedAccountNumber
          )
        }
      }

      describe("when accountNumber is too long") {
        it("throws an MalformedAccountNumber error") {
          val e = intercept[BankAccountValidationFailure] {
            buildBankAccount(accountNumber = "123456789012345678")
          }

          assert(
            e.fieldErrors("accountNumber")
              .validationMessage == BankAccountFailureReason.MalformedAccountNumber
          )
        }
      }

      describe("when accountNumber is all zeros") {
        it("throws an InvalidAllZerosAccountNumber error lower limit") {
          val e = intercept[BankAccountValidationFailure] {
            buildBankAccount(accountNumber = "00000")
          }

          assert(
            e.fieldErrors("accountNumber")
              .validationMessage == BankAccountFailureReason.InvalidAllZerosAccountNumber
          )
        }
        it("throws an InvalidAllZerosAccountNumber error upper limit") {
          val e = intercept[BankAccountValidationFailure] {
            buildBankAccount(accountNumber = "00000000000000000")
          }

          assert(
            e.fieldErrors("accountNumber")
              .validationMessage == BankAccountFailureReason.InvalidAllZerosAccountNumber
          )
        }
      }
    }
  }
