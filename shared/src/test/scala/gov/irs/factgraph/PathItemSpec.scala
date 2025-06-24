package gov.irs.factgraph

import org.scalatest.funspec.AnyFunSpec

class PathItemSpec extends AnyFunSpec:
  describe("PathItem") {
    val uuidStr = "00000000-0000-0000-0000-000000000000"
    describe("$apply") {
      it("parses string representations of path items") {
        assert(PathItem("test") == PathItem.Child(Symbol("test")))
        assert(
          PathItem(s"#$uuidStr") == PathItem.Member(
            java.util.UUID.fromString(uuidStr)
          )
        )
        assert(PathItem("*") == PathItem.Wildcard)
        assert(PathItem("?") == PathItem.Unknown)
        assert(PathItem("..") == PathItem.Parent)
      }
    }

    describe(".toString") {
      it("translates the path item to a string") {
        assert(PathItem.Child(Symbol("test")).toString == "test")
        assert(
          PathItem
            .Member(java.util.UUID.fromString(uuidStr))
            .toString == s"#$uuidStr"
        )
        assert(PathItem.Wildcard.toString == "*")
        assert(PathItem.Unknown.toString == "?")
        assert(PathItem.Parent.toString == "..")
      }
    }

    describe(".isAbstract") {
      it("Child and Wildcard are considered abstract") {
        assert(PathItem("test").isAbstract)
        assert(PathItem.Wildcard.isAbstract)
      }

      it("other path items are not") {
        assert(!PathItem(s"#$uuidStr").isAbstract)
        assert(!PathItem.Unknown.isAbstract)
        assert(!PathItem.Parent.isAbstract)
      }
    }

    describe(".isKnown") {
      it("Child and Member are considered known") {
        assert(PathItem("test").isKnown)
        assert(PathItem(s"#$uuidStr").isKnown)
      }

      it("other path items are not") {
        assert(!PathItem.Wildcard.isKnown)
        assert(!PathItem.Unknown.isKnown)
        assert(!PathItem.Parent.isKnown)
      }
    }

    describe(".asAbstract") {
      it("transforms all collection item references into wildcards") {
        assert(PathItem(s"#$uuidStr").asAbstract == PathItem.Wildcard)
        assert(PathItem.Wildcard.asAbstract == PathItem.Wildcard)
        assert(PathItem.Unknown.asAbstract == PathItem.Wildcard)
      }

      it("leaves other items alone") {
        assert(PathItem("test").asAbstract == PathItem("test"))
        assert(PathItem.Parent.asAbstract == PathItem.Parent)
      }
    }
  }
