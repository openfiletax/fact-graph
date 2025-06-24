package gov.irs.factgraph.compnodes

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.*
import gov.irs.factgraph.definitions.fact.*
import gov.irs.factgraph.monads.Result

class StringNodeSpec extends AnyFunSpec:
  describe("StringNode") {
    describe("$apply") {
      it("creates nodes from values") {
        val node = StringNode("yo")
        assert(node.get(0) == Result.Complete("yo"))
      }
    }

    describe("$fromDerivedConfig") {
      it("parses config") {
        val node = CompNode
          .fromDerivedConfig(
            new CompNodeConfigElement(
              "String",
              Seq.empty,
              CommonOptionConfigTraits.value("yo")
            )
          )
          .asInstanceOf[StringNode]
        assert(node.get(0) == Result.Complete("yo"))
      }
    }

    describe(".switch") {
      it("can be used inside a switch statement") {
        val config = new CompNodeConfigElement(
          "Switch",
          Seq(
            new CompNodeConfigElement(
              "Case",
              Seq(
                new CompNodeConfigElement(
                  "When",
                  Seq(new CompNodeConfigElement("False"))
                ),
                new CompNodeConfigElement(
                  "Then",
                  Seq(
                    new CompNodeConfigElement(
                      "String",
                      Seq.empty,
                      CommonOptionConfigTraits.value("Larry")
                    )
                  )
                )
              )
            ),
            new CompNodeConfigElement(
              "Case",
              Seq(
                new CompNodeConfigElement(
                  "When",
                  Seq(new CompNodeConfigElement("True"))
                ),
                new CompNodeConfigElement(
                  "Then",
                  Seq(
                    new CompNodeConfigElement(
                      "String",
                      Seq.empty,
                      CommonOptionConfigTraits.value("Moe")
                    )
                  )
                )
              )
            )
          )
        )
        val node = CompNode.fromDerivedConfig(config)

        assert(node.get(0) == Result.Complete("Moe"))
      }
    }

    describe(".dependency") {
      val dictionary = FactDictionary()
      FactDefinition.fromConfig(
        FactConfigElement(
          "/value",
          None,
          Some(
            new CompNodeConfigElement(
              "String",
              Seq.empty,
              CommonOptionConfigTraits.value("Hello world")
            )
          ),
          None
        )
      )(using dictionary)

      FactDefinition.fromConfig(
        FactConfigElement(
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
      )(using dictionary)

      val graph = Graph(dictionary)
      val dependent = graph(Path("/dependent"))(0).get

      it("can be depended on by another fact") {
        assert(dependent.value.isInstanceOf[StringNode])
        assert(dependent.get(0) == Result.Complete("Hello world"))
      }
    }

    describe(".fromExpression") {
      it("can create a new node with an expression") {
        val node = StringNode(Expression.Constant(None))
        val newNode =
          node.fromExpression(Expression.Constant(Some("Hello world")))

        assert(newNode.get(0) == Result.Complete("Hello world"))
      }
    }

    describe("$writablefromDerivedConfig") {
      it("can read and write a value") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("String")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set("Hello world")
        graph.save()

        assert(fact.get(0) == Result.Complete("Hello world"))
      }
    }
  }
