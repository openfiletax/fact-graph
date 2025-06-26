package gov.irs.factgraph.compnodes

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.*
import gov.irs.factgraph.definitions.FactDictionaryConfigElement
import gov.irs.factgraph.definitions.fact.*
import gov.irs.factgraph.monads.Result

class IntNodeSpec extends AnyFunSpec:
  describe("IntNode") {
    describe("$apply") {
      it("creates nodes from values") {
        val node = IntNode(123)
        assert(node.get(0) == Result.Complete(123))
      }
    }

    describe("$fromDerivedConfig") {
      it("parses config") {
        val config = new CompNodeConfigElement(
          "Int",
          Seq.empty,
          CommonOptionConfigTraits.value("123")
        )
        val node = CompNode.fromDerivedConfig(config).asInstanceOf[IntNode]
        assert(node.get(0) == Result.Complete(123))
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
                      "Int",
                      Seq.empty,
                      CommonOptionConfigTraits.value("1")
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
                      "Int",
                      Seq.empty,
                      CommonOptionConfigTraits.value("2")
                    )
                  )
                )
              )
            )
          )
        )
        val node = CompNode.fromDerivedConfig(config)

        assert(node.get(0) == Result.Complete(2))
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
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("42")
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
        assert(dependent.value.isInstanceOf[IntNode])
        assert(dependent.get(0) == Result.Complete(42))
      }
    }

    describe(".fromExpression") {
      it("can create a new node with an expression") {
        val node = IntNode(Expression.Constant(None))
        val newNode = node.fromExpression(Expression.Constant(Some(123)))

        assert(newNode.get(0) == Result.Complete(123))
      }
    }

    describe("$writablefromDerivedConfig") {
      it("can read and write a Byte value") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Int")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)
        val byte: Byte = 42
        fact.set(byte)
        graph.save()

        assert(fact.get(0) == Result.Complete(byte))
      }

      it("can read and write a Short value") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Int")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        val short: Short = 300
        fact.set(short)
        graph.save()

        assert(fact.get(0) == Result.Complete(short))
      }

      it("can read and write a Int value") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Int")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        val int: Int = 42000
        fact.set(int)
        graph.save()

        assert(fact.get(0) == Result.Complete(int))
      }

    }
  }
