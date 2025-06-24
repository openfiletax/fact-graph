package gov.irs.factgraph.util

import org.scalatest.funspec.AnyFunSpec

import gov.irs.factgraph.util.Math.gcd

class MathSpec extends AnyFunSpec:
  describe("gcd()") {
    it("finds the greatest common denominator") {
      assert(gcd(0, 1) == 1)
      assert(gcd(1, 2) == 1)
      assert(gcd(12, 9) == 3)
      assert(gcd(18, 81) == 9)
    }

    it("always retrns a positive denominator") {
      assert(gcd(-18, 81) == 9)
      assert(gcd(18, -81) == 9)
      assert(gcd(-18, -81) == 9)
    }
  }
