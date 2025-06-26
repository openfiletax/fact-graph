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

class AllSpec extends AnyFunSpec:
  describe("All") {
    it("returns true if all inputs are true") {
      val config = new CompNodeConfigElement(
        "All",
        Seq(
          new CompNodeConfigElement("True"),
          new CompNodeConfigElement("True"),
          new CompNodeConfigElement("True")
        )
      )
      val node = CompNode.fromDerivedConfig(config)
      assert(node.get(0) == Result.Complete(true))
    }

    it("returns false if any input is false") {
      val config = new CompNodeConfigElement(
        "All",
        Seq(
          new CompNodeConfigElement("True"),
          new CompNodeConfigElement("True"),
          new CompNodeConfigElement("False")
        )
      )
      val node = CompNode.fromDerivedConfig(config)
      assert(node.get(0) == Result.Complete(false))
    }

    it("stops evaluating after first false child") {
      var canary = false

      All(
        List(
          BooleanNode(false),
          BooleanNode(canaryExpr {
            canary = true
          })
        )
      ).get(0)

      assert(!canary)

      All(
        List(
          BooleanNode(true),
          BooleanNode(false),
          BooleanNode(canaryExpr {
            canary = true
          })
        )
      ).get(0)
    }

    it("only accepts boolean inputs") {
      val config = new CompNodeConfigElement(
        "All",
        Seq(
          CompNodeConfigElement(
            "String",
            Seq.empty,
            CommonOptionConfigTraits.value("Hello")
          ),
          CompNodeConfigElement(
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
      it("only explains a complete, false input if one is present") {
        val node = All(
          List(
            BooleanNode(true),
            BooleanNode(false),
            BooleanNode(false),
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
        val node = All(
          List(
            BooleanNode(true),
            BooleanNode(placeholderExpr),
            BooleanNode(placeholderFalseExpr),
            BooleanNode(incompleteExpr)
          )
        )
        assert(node.explain(0).children.length == 4)
      }
    }
  }
