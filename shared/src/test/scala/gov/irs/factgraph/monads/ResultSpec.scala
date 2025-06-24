package gov.irs.factgraph.monads

import org.scalatest.funspec.AnyFunSpec

import scala.annotation.unused

class ResultSpec extends AnyFunSpec:
  describe("Result") {
    describe(".complete") {
      it(
        "returns whether the result is complete (Placeholders are incomplete)"
      ) {
        assert(Result.Complete(true).complete)
        assert(!Result.Placeholder(true).complete)
        assert(!Result.Incomplete.complete)
      }
    }

    describe(".value") {
      it("returns an optional value") {
        assert(Result.Complete(true).value.contains(true))
        assert(Result.Placeholder(true).value.contains(true))
        assert(Result.Incomplete.value.isEmpty)
      }
    }

    describe(".get") {
      it("returns the value if available") {
        assert(Result.Complete(true).get)
        assert(Result.Placeholder(true).get)
      }

      it("throws an exception if the result is Incomplete") {
        assertThrows[NoSuchElementException] {
          Result.Incomplete.get
        }
      }
    }

    describe(".orElse") {
      it("returns the result if available, without calling default") {
        var canary = false
        assert(
          Result.Complete(1).orElse({ canary = true; 2 }) ==
            Result.Complete(1)
        )
        assert(
          Result.Placeholder(1).orElse({ canary = true; 2 }) ==
            Result.Placeholder(1)
        )
        assert(!canary)
      }

      it("returns the default if the result is incomplete") {
        assert(Result.Incomplete.orElse({ 2 }) == Result.Placeholder(2))
      }
    }

    describe(".getOrElse") {
      it("returns the result if available, without calling default") {
        var canary = false
        assert(Result.Complete(1).getOrElse({ canary = true; 2 }) == 1)
        assert(Result.Placeholder(1).getOrElse({ canary = true; 2 }) == 1)
        assert(!canary)
      }

      it("returns the default if the result is incomplete") {
        assert(Result.Incomplete.getOrElse({ 2 }) == 2)
      }
    }

    describe(".asPlaceholder") {
      it("changes Complete results into Placeholders") {
        assert(Result.Complete(true).asPlaceholder == Result.Placeholder(true))
      }

      it("leaves Placeholders and Incompletes alone") {
        assert(
          Result.Placeholder(true).asPlaceholder ==
            Result.Placeholder(true)
        )
        assert(Result.Incomplete.asPlaceholder == Result.Incomplete)
      }
    }

    describe(".map") {
      val f = (i: Int) => i + 1

      it("applies the function to the value") {
        assert(Result.Complete(1).map(f) == Result.Complete(2))
        assert(Result.Placeholder(1).map(f) == Result.Placeholder(2))
        assert(Result.Incomplete.map(f) == Result.Incomplete)
      }
    }

    describe(".flatMap") {
      val fComplete = (i: Int) => Result.Complete(i + 1)
      val fPlaceholder = (i: Int) => Result.Placeholder(i + 1)
      val fIncomplete = (i: Int) => Result.Incomplete

      describe("when the result is complete") {
        val result = Result.Complete(1)

        it("returns the result of the function") {
          assert(result.flatMap(fComplete) == Result.Complete(2))
          assert(result.flatMap(fPlaceholder) == Result.Placeholder(2))
          assert(result.flatMap(fIncomplete) == Result.Incomplete)
        }
      }

      describe("when the result is a placeholder") {
        val result = Result.Placeholder(1)

        it("makes the result of the function a placeholder too") {
          assert(result.flatMap(fComplete) == Result.Placeholder(2))
          assert(result.flatMap(fPlaceholder) == Result.Placeholder(2))
          assert(result.flatMap(fIncomplete) == Result.Incomplete)
        }
      }

      describe("when the result is incomplete") {
        val result = Result.Incomplete

        it("always returns an incomplete result") {
          assert(result.flatMap(fComplete) == Result.Incomplete)
          assert(result.flatMap(fPlaceholder) == Result.Incomplete)
          assert(result.flatMap(fIncomplete) == Result.Incomplete)
        }
      }
    }

    describe(".foreach") {
      describe("when the result is complete") {
        val result = Result.Complete(1)

        it("calls the function") {
          var sum = 0
          result.foreach(i => sum += i)
          assert(sum == 1)
        }
      }

      describe("when the result is a placeholder") {
        val result = Result.Placeholder(1)

        it("calls the function") {
          var sum = 0
          result.foreach(i => sum += i)
          assert(sum == 1)
        }
      }

      describe("when the result is incomplete") {
        val result: Result[Int] = Result.Incomplete

        it("does nothing") {
          var sum = 0
          result.foreach(i => sum += i)
          assert(sum == 0)
        }
      }
    }

    describe("$unapply") {
      it("returns a tuple containing the value and the completeness") {
        assert(Result.unapply(Result.Complete(1)) == Some(1, true))
        assert(Result.unapply(Result.Placeholder(2)) == Some(2, false))
        assert(Result.unapply(Result.Incomplete).isEmpty)
      }
    }
  }
