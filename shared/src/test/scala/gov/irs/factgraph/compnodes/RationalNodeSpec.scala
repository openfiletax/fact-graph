package gov.irs.factgraph.compnodes

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.*
import gov.irs.factgraph.definitions.fact.*
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.Rational

class RationalNodeSpec extends AnyFunSpec:
  describe("RationalNode") {
    describe("$apply") {
      it("creates nodes from values") {
        val node = RationalNode(Rational("2/3"))
        assert(node.get(0) == Result.Complete(Rational("2/3")))
      }
    }

    describe("$fromDerivedConfig") {
      it("parses config") {
        val config = new CompNodeConfigElement(
          "Rational",
          Seq.empty,
          CommonOptionConfigTraits.value("2/3")
        )
        val node = CompNode.fromDerivedConfig(config).asInstanceOf[RationalNode]
        assert(node.get(0) == Result.Complete(Rational("2/3")))
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
                      "Rational",
                      Seq.empty,
                      CommonOptionConfigTraits.value("1/2")
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
                      "Rational",
                      Seq.empty,
                      CommonOptionConfigTraits.value("2/3")
                    )
                  )
                )
              )
            )
          )
        )
        val node = CompNode.fromDerivedConfig(config)

        assert(node.get(0) == Result.Complete(Rational("2/3")))
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
              "Rational",
              Seq.empty,
              CommonOptionConfigTraits.value("1/2")
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
        assert(dependent.value.isInstanceOf[RationalNode])
        assert(dependent.get(0) == Result.Complete(Rational("1/2")))
      }
    }

    describe(".fromExpression") {
      it("can create a new node with an expression") {
        val node = RationalNode(Expression.Constant(None))
        val newNode =
          node.fromExpression(Expression.Constant(Some(Rational("1/2"))))

        assert(newNode.get(0) == Result.Complete(Rational("1/2")))
      }
    }

    describe("$writablefromDerivedConfig") {
      it("can read and write a value") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Rational")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(Rational("1/2"))
        graph.save()

        assert(fact.get(0) == Result.Complete(Rational("1/2")))
      }
    }
  }
