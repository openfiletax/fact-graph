package gov.irs.factgraph.compnodes

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.*
import gov.irs.factgraph.definitions.fact.{
  CommonOptionConfigTraits,
  CompNodeConfigElement,
  FactConfigElement,
  WritableConfigElement
}
import gov.irs.factgraph.monads.Result

class BooleanNodeSpec extends AnyFunSpec:
  describe("BooleanNode") {
    describe("$apply") {
      it("creates nodes from values") {
        val trueNode = BooleanNode(true)
        assert(trueNode.get(0) == Result.Complete(true))

        val falseNode = BooleanNode(false)
        assert(falseNode.get(0) == Result.Complete(false))
      }

      it("recycles nodes") {
        val trueNode = BooleanNode(true)
        val anotherTrueNode = BooleanNode(true)

        assert(trueNode eq anotherTrueNode)
      }
    }

    describe("$fromDerivedConfig") {
      it("runs through config") {
        val trueNode = CompNode
          .fromDerivedConfig(new CompNodeConfigElement("True"))
          .asInstanceOf[BooleanNode]
        assert(trueNode.get(0) == Result.Complete(true))

        val falseNode = CompNode
          .fromDerivedConfig(new CompNodeConfigElement("False"))
          .asInstanceOf[BooleanNode]
        assert(falseNode.get(0) == Result.Complete(false))
      }
    }

    describe(".switch") {
      it("can be used inside a switch statement with config") {
        val config = new CompNodeConfigElement(
          "Switch",
          Seq(
            new CompNodeConfigElement(
              "Case",
              Seq(
                new CompNodeConfigElement(
                  "When",
                  Seq(
                    new CompNodeConfigElement("False")
                  )
                ),
                new CompNodeConfigElement(
                  "Then",
                  Seq(
                    new CompNodeConfigElement("False")
                  )
                )
              )
            ),
            new CompNodeConfigElement(
              "Case",
              Seq(
                new CompNodeConfigElement(
                  "When",
                  Seq(
                    new CompNodeConfigElement("True")
                  )
                ),
                new CompNodeConfigElement(
                  "Then",
                  Seq(
                    new CompNodeConfigElement("True")
                  )
                )
              )
            )
          )
        )
        val node = CompNode.fromDerivedConfig(config)
        assert(node.get(0) == Result.Complete(true))
      }

    }

    describe(".dependency") {
      val dictionary = FactDictionary()
      val config = FactConfigElement(
        "/value",
        None,
        Some(new CompNodeConfigElement("True")),
        None
      )
      FactDefinition.fromConfig(config)(using dictionary)

      val secondConfig = FactConfigElement(
        "/dependent",
        None,
        Some(
          new CompNodeConfigElement(
            "Dependency",
            Seq.empty,
            CommonOptionConfigTraits.path("../value")
          )
        ),
        None
      )
      FactDefinition.fromConfig(secondConfig)(using dictionary)

      val graph = Graph(dictionary)
      val dependent = graph(Path("/dependent"))(0).get

      it("can be depended on by another fact") {
        assert(dependent.value.isInstanceOf[BooleanNode])
        assert(dependent.get(0) == Result.Complete(true))
      }
    }

    describe(".fromExpression") {
      it("can create a new node with an expression") {
        val node = BooleanNode(Expression.Constant(None))
        val newNode = node.fromExpression(Expression.Constant(Some(true)))

        assert(newNode.get(0) == Result.Complete(true))
      }
    }

    describe("$writablefromDerivedConfig") {
      it("can read and write a value") {
        val dictionary = FactDictionary()
        val config = FactConfigElement(
          "/test",
          Some(new WritableConfigElement("Boolean")),
          None,
          None
        )
        FactDefinition.fromConfig(config)(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(true)
        graph.save()

        assert(fact.get(0) == Result.Complete(true))
      }
    }
  }
