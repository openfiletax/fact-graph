package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.{
  CommonOptionConfigTraits,
  CompNodeConfigElement
}
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.{
  Explanation,
  FactDictionary,
  canaryExpr,
  incompleteExpr,
  placeholderExpr,
  placeholderFalseExpr
}
import gov.irs.factgraph.monads.Result

class AnySpec extends AnyFunSpec:
  describe("Any") {
    it("returns true if any input is true") {
      val config = new CompNodeConfigElement(
        "Any",
        Seq(
          new CompNodeConfigElement("False"),
          new CompNodeConfigElement("False"),
          new CompNodeConfigElement("True")
        )
      )
      val node = CompNode.fromDerivedConfig(config)
      assert(node.get(0) == Result.Complete(true))
    }

    it("returns false only if all inputs are false") {
      val config = new CompNodeConfigElement(
        "Any",
        Seq(
          new CompNodeConfigElement("False"),
          new CompNodeConfigElement("False"),
          new CompNodeConfigElement("False")
        )
      )
      val node = CompNode.fromDerivedConfig(config)
      assert(node.get(0) == Result.Complete(false))
    }

    it("stops evaluating after first true, complete result") {
      var canary = false

      Any(
        List(
          BooleanNode(true),
          BooleanNode(canaryExpr {
            canary = true
          })
        )
      ).get(0)

      assert(!canary)

      Any(
        List(
          BooleanNode(false),
          BooleanNode(true),
          BooleanNode(canaryExpr {
            canary = true
          })
        )
      ).get(0)
    }

    it("incomplete values don't interfere with truthy values") {
      val test1 = Any(
        List(
          BooleanNode(incompleteExpr),
          BooleanNode(true)
        )
      ).get(0)

      assert(test1 == Result.Complete(true))

      val test2 = Any(
        List(
          BooleanNode(incompleteExpr),
          BooleanNode(incompleteExpr),
          BooleanNode(placeholderExpr),
          BooleanNode(incompleteExpr)
        )
      ).get(0)

      assert(test2 == Result.Placeholder(true))

      val test3 = Any(
        List(
          BooleanNode(incompleteExpr),
          BooleanNode(incompleteExpr),
          BooleanNode(incompleteExpr)
        )
      ).get(0)

      assert(test3 == Result.Incomplete)
    }

    it("evaluates (Complete(False), Incomplete) to Incomplete") {
      val test1 = Any(
        List(
          BooleanNode(false),
          BooleanNode(incompleteExpr)
        )
      ).get(0)

      assert(test1 == Result.Incomplete)

      val test2 = Any(
        List(
          BooleanNode(incompleteExpr),
          BooleanNode(false)
        )
      ).get(0)

      assert(test2 == Result.Incomplete)
    }

    it("evaluates (Placeholder(False), Incomplete) to Incomplete") {
      val test1 = Any(
        List(
          BooleanNode(placeholderFalseExpr),
          BooleanNode(incompleteExpr)
        )
      ).get(0)

      assert(test1 == Result.Incomplete)

      val test2 = Any(
        List(
          BooleanNode(incompleteExpr),
          BooleanNode(placeholderFalseExpr)
        )
      ).get(0)

      assert(test2 == Result.Incomplete)
    }

    it(
      "evaluates (Placeholder(False), Complete(False)) to Placeholder(False)"
    ) {
      val test1 = Any(
        List(
          BooleanNode(placeholderFalseExpr),
          BooleanNode(false)
        )
      ).get(0)

      assert(test1 == Result.Placeholder(false))

      val test2 = Any(
        List(
          BooleanNode(false),
          BooleanNode(placeholderFalseExpr)
        )
      ).get(0)

      assert(test2 == Result.Placeholder(false))
    }

    it(
      "evaluates (Placeholder(False), Placeholder(False)) to Placeholder(False)"
    ) {
      val test1 = Any(
        List(
          BooleanNode(placeholderFalseExpr),
          BooleanNode(placeholderFalseExpr)
        )
      ).get(0)

      assert(test1 == Result.Placeholder(false))
    }

    it("evaluates (Complete(False), Complete(False)) to Complete(False)") {
      val test1 = Any(
        List(
          BooleanNode(false),
          BooleanNode(false)
        )
      ).get(0)

      assert(test1 == Result.Complete(false))
    }

    it("evaluates (Placeholder(True), Incomplete) to Placeholder(True)") {
      val test1 = Any(
        List(
          BooleanNode(placeholderExpr),
          BooleanNode(incompleteExpr)
        )
      ).get(0)

      assert(test1 == Result.Placeholder(true))

      val test2 = Any(
        List(
          BooleanNode(incompleteExpr),
          BooleanNode(placeholderExpr)
        )
      ).get(0)

      assert(test2 == Result.Placeholder(true))
    }

    it(
      "evaluates (Placeholder(True), Placeholder(False)) to Placeholder(True)"
    ) {
      val test1 = Any(
        List(
          BooleanNode(placeholderExpr),
          BooleanNode(placeholderFalseExpr)
        )
      ).get(0)

      assert(test1 == Result.Placeholder(true))

      val test2 = Any(
        List(
          BooleanNode(placeholderFalseExpr),
          BooleanNode(placeholderExpr)
        )
      ).get(0)

      assert(test2 == Result.Placeholder(true))
    }

    it("evaluates (Placeholder(True), Complete(False)) to Placeholder(True)") {
      val test1 = Any(
        List(
          BooleanNode(placeholderExpr),
          BooleanNode(false)
        )
      ).get(0)

      assert(test1 == Result.Placeholder(true))

      val test2 = Any(
        List(
          BooleanNode(false),
          BooleanNode(placeholderExpr)
        )
      ).get(0)

      assert(test2 == Result.Placeholder(true))
    }

    it("only accepts boolean inputs") {
      val config = new CompNodeConfigElement(
        "Any",
        Seq(
          new CompNodeConfigElement(
            "String",
            Seq.empty,
            CommonOptionConfigTraits.value("Hello")
          ),
          new CompNodeConfigElement(
            "String",
            Seq.empty,
            CommonOptionConfigTraits.value("World")
          )
        )
      )
      assertThrows[UnsupportedOperationException] {
        CompNode.fromDerivedConfig(config)
      }
    }

    describe(".explain") {
      it("only explains a complete, true input if one is present") {
        val node = Any(
          List(
            BooleanNode(false),
            BooleanNode(true),
            BooleanNode(true),
            BooleanNode(placeholderExpr),
            BooleanNode(placeholderFalseExpr),
            BooleanNode(incompleteExpr)
          )
        )
        assert(
          node.explain(0) == Explanation.Operation(
            List(
              List(
                Explanation.Constant
              )
            )
          )
        )
      }

      it("provides exclusive explanations for all of its children otherwise") {
        val node = Any(
          List(
            BooleanNode(false),
            BooleanNode(placeholderExpr),
            BooleanNode(placeholderFalseExpr),
            BooleanNode(incompleteExpr)
          )
        )
        assert(node.explain(0).children.length == 4)
      }
    }
  }
