package gov.irs.factgraph

import org.scalatest.funspec.AnyFunSpec

class PathSpec extends AnyFunSpec:
  describe("Path") {
    val testStr = "/test0/*/?/../#00000000-0000-0000-0000-000000000000"

    describe("$apply") {
      it("parses the string representation of a path") {
        assert(Path(testStr).absolute)

        assert(
          Path(testStr).items ==
            List(
              PathItem("test0"),
              PathItem.Wildcard,
              PathItem.Unknown,
              PathItem.Parent,
              PathItem("#00000000-0000-0000-0000-000000000000")
            )
        )

        assert(!Path("test").absolute)
        assert(Path("test").items == List(PathItem("test")))
      }
    }

    describe(".toString") {
      it("returns the same string representation of a path") {
        assert(Path(testStr).toString == testStr)

        assert(Path("/").toString == "/")
        assert(Path("").toString == "")
      }

      it("does not include a trailing slash") {
        assert(Path("test/").toString == "test")
      }
    }

    describe(".++") {
      it("appends a relative path to an absolute path") {
        assert(
          Path("/item0/item1") ++ Path("item2/item3") == Path(
            "/item0/item1/item2/item3"
          )
        )
      }

      it("appends a relative path to a relative path") {
        assert(
          Path("item0/item1") ++ Path("item2/item3") == Path(
            "item0/item1/item2/item3"
          )
        )
      }

      it("appends a list of items in the depthwise order") {
        val items = List(PathItem("item2"), PathItem("item3"))
        assert(
          Path("/item0/item1") ++ items == Path("/item0/item1/item2/item3")
        )
        assert(
          Path("item0/item1") ++ items == Path("item0/item1/item2/item3")
        )
      }

      describe("when appending an absolute path to another path") {
        it("just uses the absolute path") {
          assert(
            Path("/item0/item1") ++ Path("/item2/item3") == Path("/item2/item3")
          )
        }
      }
    }

    describe(".:+") {
      it("appends a path item to an absolute path") {
        assert(
          Path("/item0/item1") :+ PathItem("item2") == Path(
            "/item0/item1/item2"
          )
        )
      }

      it("appends a path item to a relative path") {
        assert(
          Path("item0/item1") :+ PathItem("item2") == Path("item0/item1/item2")
        )
      }
    }

    describe(".parent") {
      it("removes the last item from the path") {
        assert(Path("/item0/item1").parent.contains(Path("/item0")))
        assert(Path("item0/item1").parent.contains(Path("item0")))
        assert(Path("/item0").parent.contains(Path("/")))
      }
      it("returns None if there's nothing to remove") {
        assert(Path("/").parent.isEmpty)
      }
    }

    describe(".isAbstract") {
      it("returns true for an abstract path") {
        assert(Path("/test/*").isAbstract)
      }

      it("cannot be a relative path") {
        assert(!Path("test/*").isAbstract)
      }

      it("cannot contain an unknown member") {
        assert(!Path("/test/?").isAbstract)
      }

      it("cannot backtrack") {
        assert(!Path("/../test").isAbstract)
      }

      it("cannot reference a specific collection item") {
        assert(
          !Path(
            "/test/#00000000-0000-0000-0000-000000000000"
          ).isAbstract
        )
      }
    }

    describe(".isKnown") {
      it("returns true for absolute paths containing Child and Member items") {
        assert(Path("/test/#00000000-0000-0000-0000-000000000000").isKnown)
      }

      it("cannot be a relative path") {
        assert(!Path("test").isKnown)
      }

      it("cannot contain a wildcard") {
        assert(!Path("test/*").isKnown)
      }

      it("cannot contain an unknown member") {
        assert(!Path("/test/?").isKnown)
      }

      it("cannot backtrack") {
        assert(!Path("/../test").isKnown)
      }
    }

    describe(".asAbstract") {
      it("leaves non-collection paths unchanged") {
        val paths = List(
          Path("/test"),
          Path("/test/sub"),
          Path("/test/sub/two"),
          Path("../test"),
          Path("/test/?")
        )
        for path <- paths
        do assert(path.asAbstract == path)
      }
      it("replaces collection member items with wildcards") {
        assert(
          Path(
            "/test/#68eca947-ca44-4464-b2cd-e361928d6c3d"
          ).asAbstract == Path("/test/*")
        )
        assert(
          Path(
            "/test/#68eca947-ca44-4464-b2cd-e361928d6c3d/sub"
          ).asAbstract == Path("/test/*/sub")
        )
        assert(
          Path(
            "/test/#68eca947-ca44-4464-b2cd-e361928d6c3d/sub/#68eca947-ca44-4464-b2cd-e361928d6c3d"
          ).asAbstract == Path("/test/*/sub/*")
        )
        assert(
          Path(
            "/test/#68eca947-ca44-4464-b2cd-e361928d6c3d/sub/#68eca947-ca44-4464-b2cd-e361928d6c3d/two"
          ).asAbstract == Path("/test/*/sub/*/two")
        )
      }
    }
  }
