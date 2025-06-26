package gov.irs.factgraph.monads

import org.scalatest.funspec.AnyFunSpec

class MaybeVectorSpec extends AnyFunSpec:
  val single: MaybeVector[Int] = MaybeVector(1)
  val multiple: MaybeVector[Int] = MaybeVector(Vector(1, 2, 3), true)

  describe("MaybeVector") {
    describe(".apply") {
      describe("when the vector is Single") {
        it("returns the only value regardless of the provided index") {
          assert(single(0) == 1)
          assert(single(999) == 1)
          assert(single(-999) == 1)
        }
      }

      describe("when the vector is Multiple") {
        it("returns the value at that index") {
          assert(multiple(0) == 1)
          assert(multiple(1) == 2)
          assert(multiple(2) == 3)
        }

        it("throws if the index is out of bounds") {
          assertThrows[IndexOutOfBoundsException] {
            multiple(3)
          }
        }
      }
    }

    describe(".toVector") {
      it("translates a Single to a vector of length 1") {
        assert(single.toVector == Vector(1))
      }

      it("extracts the raw vector from a Multiple") {
        assert(multiple.toVector == Vector(1, 2, 3))
      }
    }

    describe(".toList") {
      it("translates a Single to a list of length 1") {
        assert(single.toList == List(1))
      }

      it("translates a Multiple to a list") {
        assert(multiple.toList == List(1, 2, 3))
      }
    }

    describe(".length") {
      it("returns None if Single") {
        assert(single.length.isEmpty)
      }

      it("returns the length of the vector if Multiple") {
        assert(multiple.length.contains(3))
      }
    }

    describe(".complete") {
      it("Single is always complete") {
        assert(single.complete)
      }

      it("Multiple can be be complete or incomplete") {
        assert(multiple.complete)
        assert(!MaybeVector(Vector(), false).complete)
      }
    }

    describe(".map") {
      it("applies the function to all members") {
        def f(i: Int) = i + 1

        assert(single.map(f)(0) == 2)
        assert(multiple.map(f).toVector == Vector(2, 3, 4))
      }
    }

    describe(".flatMap") {
      it(
        "combines the vectors created by mapping the function to all members"
      ) {
        def f(i: Int) = MaybeVector(Vector(i - 1, i, i + 1), true)

        assert(single.flatMap(f).toVector == Vector(0, 1, 2))
        assert(
          multiple.flatMap(f).toVector ==
            Vector(0, 1, 2, 1, 2, 3, 2, 3, 4)
        )
      }
    }

    describe("$vectorizeList") {
      def f(h: Int, t: List[Int]) = (h +: t).sum

      describe("when all of the arguments are Single") {
        it("produces a Single result") {
          assert(
            MaybeVector.vectorizeList(f, single, List(single, single)) ==
              MaybeVector(3)
          )
        }
      }

      describe("when the arguments contain a Multiple") {
        it("produces a Multiple result by vectorizing the function") {
          assert(
            MaybeVector.vectorizeList(f, single, List(single, multiple)) ==
              MaybeVector(Vector(3, 4, 5), true)
          )
        }

        it("throws if the arguments have different lengths") {
          assertThrows[UnsupportedOperationException] {
            val multiple2 = MaybeVector(Vector(1, 2), true)
            MaybeVector.vectorizeList(f, single, List(multiple, multiple2))
          }
        }
      }
    }

    describe("$vectorize2") {
      def f(lhs: Int, rhs: Int) = lhs == rhs

      describe("when all of the arguments are Single") {
        it("produces a Single result") {
          assert(
            MaybeVector.vectorize2(f, single, single) ==
              MaybeVector(true)
          )
        }
      }

      describe("when the arguments contain a Multiple") {
        it("produces a Multiple result by vectorizing the function") {
          assert(
            MaybeVector.vectorize2(f, single, multiple) ==
              MaybeVector(Vector(true, false, false), true)
          )
        }

        it("throws if the arguments have different lengths") {
          assertThrows[UnsupportedOperationException] {
            val multiple2 = MaybeVector(Vector(1, 2), true)
            MaybeVector.vectorize2(f, multiple, multiple2)
          }
        }
      }
    }

    describe("$vectorizeListTuple2") {
      def f(cases: List[(Boolean, Int)]) = cases.find(_._1).get._2

      describe("when all of the tuples are Single") {
        val singleCases = List(
          (MaybeVector(false), single),
          (MaybeVector(true), MaybeVector(2))
        )

        it("produces a Single result") {
          assert(
            MaybeVector.vectorizeListTuple2(f, singleCases) ==
              MaybeVector(2)
          )
        }
      }

      describe("when the tuples have different lengths") {
        val multipleCases = List(
          (MaybeVector(Vector(true, true, false), true), single),
          (MaybeVector(Vector(false, true, true), true), multiple)
        )

        it("produces a result for each permutation") {
          assert(
            MaybeVector.vectorizeListTuple2(f, multipleCases) ==
              MaybeVector(Vector(1, 1, 3), true)
          )
        }
      }

      describe("when the lengths of the arguments in a tuple do not match") {
        val invalidCases = List(
          (MaybeVector(Vector(true, false), true), multiple)
        )

        it("throws an exception") {
          assertThrows[UnsupportedOperationException] {
            MaybeVector.vectorizeListTuple2(f, invalidCases)
          }
        }
      }
    }

    describe("$vectorizeListTuple3") {
      def f(cases: List[(Boolean, Int, Int)]) =
        val result = cases.find(_._1).get
        result._2 + result._3

      describe("when all of the tuples are Single") {
        val singleCases = List(
          (MaybeVector(false), single, MaybeVector(2)),
          (MaybeVector(true), MaybeVector(3), MaybeVector(4))
        )

        it("produces a Single result") {
          assert(
            MaybeVector.vectorizeListTuple3(f, singleCases) ==
              MaybeVector(7)
          )
        }
      }

      describe("when the tuples have different lengths") {
        val multipleCases = List(
          (MaybeVector(Vector(true, true, false), true), single, single),
          (MaybeVector(Vector(false, true, true), true), single, multiple)
        )

        it("produces a result for each permutation") {
          assert(
            MaybeVector.vectorizeListTuple3(f, multipleCases) ==
              MaybeVector(Vector(2, 2, 4), true)
          )
        }
      }

      describe("when the lengths of the arguments in a tuple do not match") {
        val invalidCases = List(
          (MaybeVector(Vector(true, false), true), multiple, multiple)
        )

        it("throws an exception") {
          assertThrows[UnsupportedOperationException] {
            MaybeVector.vectorizeListTuple3(f, invalidCases)
          }
        }
      }
    }
  }
