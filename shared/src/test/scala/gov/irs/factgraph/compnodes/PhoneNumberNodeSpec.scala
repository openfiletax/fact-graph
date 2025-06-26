package gov.irs.factgraph.compnodes

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.*
import gov.irs.factgraph.definitions.fact.*
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.PhoneNumber

class PhoneNumberNodeSpec extends AnyFunSpec:
  describe("PhoneNumberNode") {
    describe("$apply") {
      it("creates nodes from values") {
        val node = PhoneNumberNode(PhoneNumber("+15555550100"))
        assert(node.get(0) == Result.Complete(PhoneNumber("+15555550100")))
      }
    }

    describe("$fromDerivedConfig") {
      it("parses config") {
        val config = new CompNodeConfigElement(
          "PhoneNumber",
          Seq.empty,
          CommonOptionConfigTraits.value("+15555550100")
        )
        val node =
          CompNode.fromDerivedConfig(config).asInstanceOf[PhoneNumberNode]
        assert(node.get(0) == Result.Complete(PhoneNumber("+15555550100")))
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
                      "PhoneNumber",
                      Seq.empty,
                      CommonOptionConfigTraits.value("+15555550101")
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
                      "PhoneNumber",
                      Seq.empty,
                      CommonOptionConfigTraits.value("+15555550100")
                    )
                  )
                )
              )
            )
          )
        )
        val node = CompNode.fromDerivedConfig(config)

        assert(node.get(0) == Result.Complete(PhoneNumber("+15555550100")))
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
              "PhoneNumber",
              Seq.empty,
              CommonOptionConfigTraits.value("+15555550101")
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
        assert(dependent.value.isInstanceOf[PhoneNumberNode])
        assert(dependent.get(0) == Result.Complete(PhoneNumber("+15555550101")))
      }
    }

    describe(".fromExpression") {
      it("can create a new node with an expression") {
        val node = PhoneNumberNode(Expression.Constant(None))
        val newNode =
          node.fromExpression(
            Expression.Constant(Some(PhoneNumber("+15555550101")))
          )

        assert(newNode.get(0) == Result.Complete(PhoneNumber("+15555550101")))
      }
    }

    describe("$writablefromDerivedConfig") {
      it("can read and write a value") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("PhoneNumber")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(PhoneNumber("+15555550101"))
        graph.save()

        assert(fact.get(0) == Result.Complete(PhoneNumber("+15555550101")))
      }
    }
  }
