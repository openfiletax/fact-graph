package gov.irs.factgraph.monads

import org.scalatest.funspec.AnyFunSpec

class ThunkSpec extends AnyFunSpec:
  describe("Thunk") {
    describe(".get") {
      var runCount = 0
      val f = () =>
        runCount += 1
        true
      val thunk = Thunk(f)

      it("doesn't run until get is called") {
        assert(runCount == 0)
        assert(thunk.get)
        assert(runCount == 1)
      }

      it("only runs once, no matter how many times called") {
        assert(thunk.get)
        assert(runCount == 1)
      }
    }

    describe(".map") {
      var runCount = 0
      val f = () =>
        runCount += 1
        1
      val thunk = Thunk(f)

      val f1 = (i: Int) => i + 1
      val mapped = thunk.map(f1)

      it("waits to apply the function until get is called") {
        assert(runCount == 0)
        assert(mapped.get == 2)
        assert(runCount == 1)
      }

      it("only runs once, no matter how many times called") {
        assert(mapped.get == 2)
        assert(runCount == 1)
      }
    }

    describe(".flatMap") {
      var runCount = 0
      val f = () =>
        runCount += 1
        1
      val thunk = Thunk(f)

      def f1(i: => Int) = Thunk(() => i + 1)
      val mapped = thunk.flatMap(f1)

      it("doesn't run the inner or outer function until get is called") {
        assert(runCount == 0)
        assert(mapped.get == 2)
        assert(runCount == 1)
      }

      it("only runs once, no matter how many times called") {
        assert(mapped.get == 2)
        assert(runCount == 1)
      }
    }
  }
