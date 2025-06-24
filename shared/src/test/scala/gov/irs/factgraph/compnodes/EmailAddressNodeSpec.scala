package gov.irs.factgraph.compnodes

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.*
import gov.irs.factgraph.definitions.fact.*
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.EmailAddress

class EmailAddressNodeSpec extends AnyFunSpec:
  describe("EmailAddressNode") {
    describe("$apply") {
      it("creates nodes from values") {
        val node = EmailAddressNode(EmailAddress("example@example.com"))
        assert(
          node.get(0) == Result.Complete(EmailAddress("example@example.com"))
        )
      }
    }

    describe("$fromDerivedConfig") {
      it("parses config") {
        val config = new CompNodeConfigElement(
          "EmailAddress",
          Seq.empty,
          CommonOptionConfigTraits.value("example@example.com")
        )
        val node =
          CompNode.fromDerivedConfig(config).asInstanceOf[EmailAddressNode]
        assert(
          node.get(0) == Result.Complete(EmailAddress("example@example.com"))
        )
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
                      "EmailAddress",
                      Seq.empty,
                      CommonOptionConfigTraits.value("false@example.com")
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
                      "EmailAddress",
                      Seq.empty,
                      CommonOptionConfigTraits.value("true@example.com")
                    )
                  )
                )
              )
            )
          )
        )
        val node = CompNode.fromDerivedConfig(config)

        assert(node.get(0) == Result.Complete(EmailAddress("true@example.com")))
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
              "EmailAddress",
              Seq.empty,
              CommonOptionConfigTraits.value("example@example.com")
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
        assert(dependent.value.isInstanceOf[EmailAddressNode])
        assert(
          dependent.get(0) == Result.Complete(
            EmailAddress("example@example.com")
          )
        )
      }
    }

    describe(".fromExpression") {
      it("can create a new node with an expression") {
        val node = EmailAddressNode(Expression.Constant(None))
        val newNode =
          node.fromExpression(
            Expression.Constant(Some(EmailAddress("example@example.com")))
          )

        assert(
          newNode.get(0) == Result.Complete(EmailAddress("example@example.com"))
        )
      }
    }

    describe("$writablefromDerivedConfig") {
      it("can read and write a value") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("EmailAddress")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(EmailAddress("example@example.com"))
        graph.save()

        assert(
          fact.get(0) == Result.Complete(EmailAddress("example@example.com"))
        )
      }
    }
  }
