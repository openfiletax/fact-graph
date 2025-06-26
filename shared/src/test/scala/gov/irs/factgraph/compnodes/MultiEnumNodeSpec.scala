package gov.irs.factgraph.compnodes

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.*
import gov.irs.factgraph.definitions.fact.*
import gov.irs.factgraph.definitions.meta.{
  EnumDeclarationOptionsTrait,
  EnumDeclarationTrait,
  MetaConfigTrait
}
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.persisters.InMemoryPersister
import gov.irs.factgraph.types.*

class MultiEnumNodeSpec extends AnyFunSpec {
  describe("MultiEnumNode") {
    describe("$apply") {
      it("creates nodes from values") {
        val node =
          MultiEnumNode(
            MultiEnum("test", "/enum-options")
          )
        assert(
          node.get(0) == Result.Complete(
            MultiEnum(Set("test"), "/enum-options")
          )
        )
      }
    }
  }
  describe("$fromDerivedConfig") {
    it("parses config") {
      val dictionary = FactDictionary()
      val node =
        CompNode
          .fromDerivedConfig(
            new CompNodeConfigElement(
              "MultiEnum",
              Seq.empty,
              CommonOptionConfigTraits.create(
                Seq(
                  (CommonOptionConfigTraits.ENUM_OPTIONS_PATH, "/options-path"),
                  (CommonOptionConfigTraits.VALUE, "A")
                )
              )
            )
          )(using given_Factual)(using dictionary)
          .asInstanceOf[MultiEnumNode]
      assert(
        node.get(0) == Result.Complete(
          MultiEnum(Set("A"), "/options-path")
        )
      )
    }

    it("throws an error if the optionsPath is not set") {
      val dictionary = FactDictionary();
      assertThrows[IllegalArgumentException] {
        CompNode
          .fromDerivedConfig(
            new CompNodeConfigElement(
              "Enum",
              Seq.empty,
              CommonOptionConfigTraits.create(
                Seq(
                  (CommonOptionConfigTraits.VALUE, "C")
                )
              )
            )
          )(using given_Factual)(using dictionary)
      }
    }

    it("parses config with multiple comma delimited values") {
      val dictionary = FactDictionary()
      val node =
        CompNode
          .fromDerivedConfig(
            new CompNodeConfigElement(
              "MultiEnum",
              Seq.empty,
              CommonOptionConfigTraits.create(
                Seq(
                  (CommonOptionConfigTraits.ENUM_OPTIONS_PATH, "/options-path"),
                  (CommonOptionConfigTraits.VALUE, "A,B")
                )
              )
            )
          )(using given_Factual)(using dictionary)
          .asInstanceOf[MultiEnumNode]
      assert(
        node.get(0) == Result.Complete(
          MultiEnum(Set("A", "B"), "/options-path")
        )
      )
    }

  }

  describe(".switch") {
    it("can be used inside a switch statement") {
      val dictionary = FactDictionary()
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
                    "MultiEnum",
                    Seq.empty,
                    CommonOptionConfigTraits.create(
                      Seq(
                        (
                          CommonOptionConfigTraits.ENUM_OPTIONS_PATH,
                          "/options-path"
                        ),
                        (CommonOptionConfigTraits.VALUE, "A")
                      )
                    )
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
                    "MultiEnum",
                    Seq.empty,
                    CommonOptionConfigTraits.create(
                      Seq(
                        (
                          CommonOptionConfigTraits.ENUM_OPTIONS_PATH,
                          "/options-path"
                        ),
                        (CommonOptionConfigTraits.VALUE, "B,C")
                      )
                    )
                  )
                )
              )
            )
          )
        )
      )
      val node =
        CompNode.fromDerivedConfig(config)(using given_Factual)(using
          dictionary
        )
      assert(
        node.get(0) == Result.Complete(
          MultiEnum(Set("B", "C"), "/options-path")
        )
      )
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
            "MultiEnum",
            Seq.empty,
            CommonOptionConfigTraits.create(
              Seq(
                (CommonOptionConfigTraits.ENUM_OPTIONS_PATH, "/options-path"),
                (CommonOptionConfigTraits.VALUE, "C")
              )
            )
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
      assert(dependent.value.isInstanceOf[MultiEnumNode])
      assert(
        dependent.get(0) == Result.Complete(
          MultiEnum("C", "/options-path")
        )
      )
    }
  }
  describe(".fromExpression") {
    it("can create a new node with an expression") {
      val node = MultiEnumNode(Expression.Constant(None), Path("test"))
      val newNode =
        node.fromExpression(
          Expression.Constant(Some(MultiEnum("B", "test")))
        )

      assert(
        newNode.get(0) == Result.Complete(MultiEnum("B", "test"))
      )
    }
  }

  describe("$writablefromDerivedConfig") {
    it("can read and write a value") {
      val dictionary = FactDictionary()
      FactDefinition.fromConfig(
        FactConfigElement(
          "/anotherName",
          Some(
            new WritableConfigElement(
              "MultiEnum",
              CommonOptionConfigTraits.optionsPath("/options-path")
            )
          ),
          None,
          None
        )
      )(using dictionary)

      val graph = Graph(dictionary)
      val fact = graph(Path("/anotherName"))(0).get
      assert(fact.get(0) == Result.Incomplete)

      fact.value match
        case value: MultiEnumNode =>
          fact.set(MultiEnum(Set("C", "D"), "/options-path"))

      graph.save()

      assert(
        fact.get(0) == Result.Complete(
          MultiEnum(Set("C", "D"), "/options-path")
        )
      )
    }

    it("throws an error if the optionsPath is not set") {
      val dictionary = FactDictionary();
      assertThrows[IllegalArgumentException] {

        FactDefinition.fromConfig(
          FactConfigElement(
            "/anotherName",
            Some(
              new WritableConfigElement(
                "MultiEnum"
              )
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/anotherName"))(0).get
      }
    }

    it("trims whitespace in the config") {
      val dictionary = FactDictionary()
      val node =
        CompNode
          .fromDerivedConfig(
            new CompNodeConfigElement(
              "MultiEnum",
              Seq.empty,
              CommonOptionConfigTraits.create(
                Seq(
                  (CommonOptionConfigTraits.ENUM_OPTIONS_PATH, "/options-path"),
                  (CommonOptionConfigTraits.VALUE, "A, B")
                )
              )
            )
          )(using given_Factual)(using dictionary)
          .asInstanceOf[MultiEnumNode]
      assert(
        node.get(0) == Result.Complete(
          MultiEnum(Set("A", "B"), "/options-path")
        )
      )
    }
  }

  describe("when setting a value") {
    describe("when comparing") {
      val dictionary = FactDictionary()
      it("can use out of order enum sets") {
        FactDefinition.fromConfig(
          FactConfigElement(
            "/alreadySaved",
            Some(
              new WritableConfigElement(
                "MultiEnum",
                CommonOptionConfigTraits.optionsPath("/options-path")
              )
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(
          dictionary,
          InMemoryPersister(
            "/alreadySaved" -> MultiEnum(
              Set("D", "C"),
              "/options-path"
            )
          )
        )
        val fact = graph(Path("/alreadySaved"))(0).get

        val trueNode = CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "Equal",
            Seq(
              new CompNodeConfigElement(
                "Left",
                Seq(
                  new CompNodeConfigElement(
                    "Dependency",
                    Seq.empty,
                    CommonOptionConfigTraits.path("../alreadySaved")
                  )
                ),
                Seq.empty
              ),
              new CompNodeConfigElement(
                "Right",
                Seq(
                  new CompNodeConfigElement(
                    "MultiEnum",
                    Seq.empty,
                    CommonOptionConfigTraits.create(
                      Seq(
                        (
                          CommonOptionConfigTraits.ENUM_OPTIONS_PATH,
                          "/options-path"
                        ),
                        (CommonOptionConfigTraits.VALUE, "C,D")
                      )
                    )
                  )
                )
              )
            )
          )
        )(using fact)(using dictionary)
        val falseNode = CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "Equal",
            Seq(
              new CompNodeConfigElement(
                "Left",
                Seq(
                  new CompNodeConfigElement(
                    "Dependency",
                    Seq.empty,
                    CommonOptionConfigTraits.path("../alreadySaved")
                  )
                ),
                Seq.empty
              ),
              new CompNodeConfigElement(
                "Right",
                Seq(
                  new CompNodeConfigElement(
                    "MultiEnum",
                    Seq.empty,
                    CommonOptionConfigTraits.create(
                      Seq(
                        (
                          CommonOptionConfigTraits.ENUM_OPTIONS_PATH,
                          "/options-path"
                        ),
                        (CommonOptionConfigTraits.VALUE, "C")
                      )
                    )
                  )
                )
              )
            )
          )
        )(using fact)(using dictionary)

        assert(trueNode.get(using fact)(0) == Result.Complete(true))
        assert(falseNode.get(using fact)(0) == Result.Complete(false))
      }
    }
  }
}
