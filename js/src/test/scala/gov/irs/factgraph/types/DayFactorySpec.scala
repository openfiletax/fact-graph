package gov.irs.factgraph.types

import org.scalatest.funspec.AnyFunSpec

class DayFactorySpec extends AnyFunSpec:
  describe("Day with no limits") {
    assert(DayFactory("2012-10-10").right.toString === "2012-10-10")
  }

  describe("Day with limit not exceeded") {
    assert(
      DayFactory("2010-10-10", "2010-10-11").right.toString === "2010-10-10"
    )
  }

  describe("Day with limit exceeded") {
    val errorReason = DayFactory(
      "2010-10-10",
      "2010-10-10"
    ).left.validationMessage.toUserFriendlyReason().toString()
    assert(errorReason === "ExceedsMaxLimit")
  }

  describe("Day with invalid limit set") {
    val errorReason = DayFactory(
      "2010-10-10",
      "2010/10/11"
    ).left.validationMessage.toUserFriendlyReason().toString()
    assert(errorReason === "InvalidLimit")
  }
