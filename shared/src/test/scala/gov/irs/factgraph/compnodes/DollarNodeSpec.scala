package gov.irs.factgraph.compnodes

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.*
import gov.irs.factgraph.definitions.fact.*
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.Dollar

class DollarNodeSpec extends AnyFunSpec:
  describe("DollarNode") {
    describe("$apply") {
      it("creates nodes from values") {
        val node = DollarNode(Dollar("1.23"))
        assert(node.get(0) == Result.Complete(Dollar("1.23")))
      }
    }

    describe("$fromDerivedConfig") {
      it("parses config") {
        val node =
          CompNode
            .fromDerivedConfig(
              new CompNodeConfigElement(
                "Dollar",
                Seq.empty,
                CommonOptionConfigTraits.value("1.23")
              )
            )
            .asInstanceOf[DollarNode]
        assert(node.get(0) == Result.Complete(Dollar("1.23")))
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
                      "Dollar",
                      Seq.empty,
                      CommonOptionConfigTraits.value("1.00")
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
                      "Dollar",
                      Seq.empty,
                      CommonOptionConfigTraits.value("2.00")
                    )
                  )
                )
              )
            )
          )
        )
        val node = CompNode.fromDerivedConfig(config)
        assert(node.get(0) == Result.Complete(Dollar("2.00")))
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
              "Dollar",
              Seq.empty,
              CommonOptionConfigTraits.value("1.23")
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
        assert(dependent.value.isInstanceOf[DollarNode])
        assert(dependent.get(0) == Result.Complete(Dollar("1.23")))
      }
    }

    describe(".fromExpression") {
      it("can create a new node with an expression") {
        val node = DollarNode(Expression.Constant(None))
        val newNode =
          node.fromExpression(Expression.Constant(Some(Dollar("1.23"))))

        assert(newNode.get(0) == Result.Complete(Dollar("1.23")))
      }
    }

    describe("$writablefromDerivedConfig") {
      it("can read and write a value") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Dollar")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(Dollar("1.23"))
        graph.save()

        assert(fact.get(0) == Result.Complete(Dollar("1.23")))
      }
    }
  }
