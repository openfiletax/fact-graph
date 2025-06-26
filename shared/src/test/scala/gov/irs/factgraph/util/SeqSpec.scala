package gov.irs.factgraph.util

import org.scalatest.funspec.AnyFunSpec

import gov.irs.factgraph.util.Seq.*

class SeqSpec extends AnyFunSpec:
  describe("itemsHaveSameRuntimeClass()") {
    describe("when the list is empty") {
      it("returns true") {
        assert(itemsHaveSameRuntimeClass(List()))
      }
    }
    describe("when all of the items are of the same class") {
      it("returns true") {
        assert(itemsHaveSameRuntimeClass(List(1)))
        assert(itemsHaveSameRuntimeClass(List(1, 2, 3)))
      }
    }
    describe("when the items are of different classes") {
      it("returns false") {
        assert(!itemsHaveSameRuntimeClass(List(1, 2, "yo")))
      }
    }
  }
