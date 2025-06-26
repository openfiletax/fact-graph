package gov.irs.factgraph.types

import org.scalatest.funspec.AnyFunSpec

class DollarFactorySpec extends AnyFunSpec:
  describe("Dollar with no limits") {
    assert(DollarFactory.apply("301").right === 301.00)
  }

  describe("Dollar with limit not exceeded") {
    assert(DollarFactory.apply("299", 300).right === 299.00)
  }

  describe("Dollar with max limit exceeded") {
    assert(
      DollarFactory
        .apply("301", 300)
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString() === "ExceedsMaxLimit"
    )
  }

  describe("Dollar with min limit exceeded") {
    assert(
      DollarFactory
        .apply("10", 300, 100)
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString() === "ExceedsMinLimit"
    )
  }

  describe("Dollar with min limit exceeded with decimals") {
    assert(
      DollarFactory
        .apply("-10.00", 300, 0)
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString() === "ExceedsMinLimit"
    )
  }

  describe("Dollar with limit exceeded and non integer string value") {
    assert(
      DollarFactory
        .apply("test", 300)
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString() === "InvalidCharacters"
    )
  }

  describe("Dollar with decimal Min limit exceeded") {
    assert(
      DollarFactory
        .apply("101.23", 200.00, 101.25)
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString() === "ExceedsMinLimit"
    )
  }

  describe("Dollar with decimal Max limit exceeded") {
    assert(
      DollarFactory
        .apply("84.45", 84.43)
        .left
        .validationMessage
        .toUserFriendlyReason()
        .toString() === "ExceedsMaxLimit"
    )
  }
