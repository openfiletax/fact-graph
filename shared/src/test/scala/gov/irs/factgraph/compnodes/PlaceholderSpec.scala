package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{
  Explanation,
  Expression,
  completeExpr,
  incompleteExpr,
  placeholderExpr
}
import gov.irs.factgraph.definitions.fact.*
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.FactDefinition
import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.Graph

class PlaceholderSpec extends AnyFunSpec:

  describe("Placeholder") {
    it("can be created from config") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "Placeholder",
          Seq(
            new CompNodeConfigElement(
              "Source",
              Seq(
                new CompNodeConfigElement(
                  "Int",
                  Seq.empty,
                  CommonOptionConfigTraits.value("1")
                )
              )
            ),
            new CompNodeConfigElement(
              "Default",
              Seq(
                new CompNodeConfigElement(
                  "Int",
                  Seq.empty,
                  CommonOptionConfigTraits.value("2")
                )
              )
            )
          )
        )
      )
      assert(node.get(0) == Result.Complete(1))
    }

    it("requires both inputs to be of the same type") {
      assertThrows[UnsupportedOperationException] {
        CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "Placeholder",
            Seq(
              new CompNodeConfigElement(
                "Source",
                Seq(
                  new CompNodeConfigElement(
                    "Int",
                    Seq.empty,
                    CommonOptionConfigTraits.value("1")
                  )
                )
              ),
              new CompNodeConfigElement(
                "Default",
                Seq(
                  CompNodeConfigElement(
                    "Dollar",
                    Seq.empty,
                    CommonOptionConfigTraits.value("1.00")
                  )
                )
              )
            )
          )
        )
      }
    }

    it("does nothing if the source is complete") {
      val node = Placeholder(BooleanNode(completeExpr), BooleanNode.False.node)
      assert(node.get(0) == Result.Complete(true))
    }

    it("does nothing if the source already has a placeholder value") {
      val node =
        Placeholder(BooleanNode(placeholderExpr), BooleanNode.False.node)
      assert(node.get(0) == Result.Placeholder(true))
    }

    it("uses the default if the source is incomplete") {
      val node =
        Placeholder(BooleanNode(incompleteExpr), BooleanNode.False.node)
      assert(node.get(0) == Result.Placeholder(false))
    }

    describe("when wrapping a writable") {
      it("can be replaced by a writable value") {
        val dictionary = FactDictionary()
        val definition = FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("String")
            ),
            None,
            Some(
              CompNodeConfigElement(
                "String",
                Seq.empty,
                CommonOptionConfigTraits.value("Placeholder")
              )
            )
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph.get("/test")

        assert(fact == Result.Placeholder("Placeholder"))

        graph.set("/test", "Hello world")
        graph.save()

        assert(graph.get("/test") == Result.Complete("Hello world"))
      }
    }

    describe("when wrapping a derivable") {
      it("can NOT be replaced by a writable value") {
        val dictionary = FactDictionary()
        val definition = FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            None,
            Some(
              new CompNodeConfigElement(
                "Add",
                Seq(
                  CompNodeConfigElement(
                    "Int",
                    Seq.empty,
                    CommonOptionConfigTraits.value("1")
                  ),
                  CompNodeConfigElement(
                    "Int",
                    Seq.empty,
                    CommonOptionConfigTraits.value("2")
                  ),
                  CompNodeConfigElement(
                    "Int",
                    Seq.empty,
                    CommonOptionConfigTraits.value("3")
                  )
                )
              )
            ),
            Some(
              CompNodeConfigElement(
                "Int",
                Seq.empty,
                CommonOptionConfigTraits.value("0")
              )
            )
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph.get("/test")

        intercept[Exception] {
          graph.set("/test", true)
        }
      }
    }
  }

  describe("PlaceholderOperator") {
    it("overrides explain to only explain source value") {
      val op = summon[PlaceholderOperator[Int]]
      val lhs = completeExpr
      val rhs = completeExpr

      assert(
        op.explain(lhs, rhs)(0) == Explanation.Operation(
          List(
            List(
              Explanation.Constant
            )
          )
        )
      )
    }
  }
