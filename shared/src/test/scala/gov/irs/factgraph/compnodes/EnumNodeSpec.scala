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
import gov.irs.factgraph.limits.ContainsLimit

class EnumNodeSpec extends AnyFunSpec {
  describe("EnumNode") {
    describe("$apply") {
      it("creates nodes from values") {
        val node =
          EnumNode(Enum("test", "/enum-options"))
        assert(
          node.get(0) == Result.Complete(
            Enum("test", "/enum-options")
          )
        )
      }
    }
  }
  describe("$fromDerivedConfig") {
    it("parses config") {
      val dictionary = FactDictionary();
      val node =
        CompNode
          .fromDerivedConfig(
            new CompNodeConfigElement(
              "Enum",
              Seq.empty,
              CommonOptionConfigTraits.create(
                Seq(
                  (CommonOptionConfigTraits.ENUM_OPTIONS_PATH, "/options-path"),
                  (CommonOptionConfigTraits.VALUE, "A")
                )
              )
            )
          )(using given_Factual)(using dictionary)
          .asInstanceOf[EnumNode]
      assert(
        node.get(0) == Result.Complete(
          Enum("A", "/options-path")
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
  }
  describe(".switch") {
    it("can be used inside a switch statement") {
      val dictionary = FactDictionary();

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
                    "Enum",
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
                    "Enum",
                    Seq.empty,
                    CommonOptionConfigTraits.create(
                      Seq(
                        (
                          CommonOptionConfigTraits.ENUM_OPTIONS_PATH,
                          "/options-path"
                        ),
                        (CommonOptionConfigTraits.VALUE, "B")
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
          Enum("B", "/options-path")
        )
      )
    }
  }

  describe(".dependency") {
    val dictionary = FactDictionary();

    FactDefinition.fromConfig(
      FactConfigElement(
        "/value",
        None,
        Some(
          new CompNodeConfigElement(
            "Enum",
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
      assert(dependent.value.isInstanceOf[EnumNode])
      assert(
        dependent.get(0) == Result.Complete(
          Enum("C", "/options-path")
        )
      )
    }
  }

  describe(".fromExpression") {
    it("can create a new node with an expression") {
      val node = EnumNode(Expression.Constant(None), "/options-path")
      val newNode =
        node.fromExpression(
          Expression.Constant(Some(Enum("B", "/options-path")))
        )

      assert(
        newNode.get(0) == Result.Complete(Enum("B", "/options-path"))
      )
    }
  }

  describe("$writablefromDerivedConfig") {
    it("can read and write a value") {
      val dictionary = FactDictionary();
      FactDefinition.fromConfig(
        FactConfigElement(
          "/options-path",
          None,
          Some(
            new CompNodeConfigElement(
              "EnumOptions",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("A")
                ),
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("B")
                ),
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("C")
                ),
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("D")
                )
              ),
              Seq.empty
            )
          ),
          None
        )
      )(using dictionary)
      FactDefinition.fromConfig(
        FactConfigElement(
          "/anotherName",
          Some(
            new WritableConfigElement(
              "Enum",
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
        case value: EnumNode => fact.set(Enum("D", "/options-path"))

      graph.save()

      assert(
        fact.get(0) == Result.Complete(
          Enum("D", "/options-path")
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
                "Enum"
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

  }

  describe("when setting a value") {

    describe("when comparing") {
      it("can use out of order enum sets") {
        val dictionary = FactDictionary();
        FactDefinition.fromConfig(
          FactConfigElement(
            "/options-path",
            None,
            Some(
              new CompNodeConfigElement(
                "EnumOptions",
                Seq(
                  new CompNodeConfigElement(
                    "String",
                    Seq.empty,
                    CommonOptionConfigTraits.value("A")
                  ),
                  new CompNodeConfigElement(
                    "String",
                    Seq.empty,
                    CommonOptionConfigTraits.value("B")
                  ),
                  new CompNodeConfigElement(
                    "String",
                    Seq.empty,
                    CommonOptionConfigTraits.value("C")
                  ),
                  new CompNodeConfigElement(
                    "String",
                    Seq.empty,
                    CommonOptionConfigTraits.value("D")
                  )
                ),
                Seq.empty
              )
            ),
            None
          )
        )(using dictionary)
        FactDefinition.fromConfig(
          FactConfigElement(
            "/alreadySaved",
            Some(
              new WritableConfigElement(
                "Enum",
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
            "/alreadySaved" -> Enum(
              Some("D"),
              "/options-path"
            )
          )
        )
        val fact = graph(Path("/alreadySaved"))(0).get

        val node = CompNode.fromDerivedConfig(
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
                    "Enum",
                    Seq.empty,
                    CommonOptionConfigTraits.create(
                      Seq(
                        (
                          CommonOptionConfigTraits.ENUM_OPTIONS_PATH,
                          "/options-path"
                        ),
                        (CommonOptionConfigTraits.VALUE, "D")
                      )
                    )
                  )
                )
              )
            )
          )
        )(using fact)(using dictionary)

        assert(node.get(using fact)(0) == Result.Complete(true))
      }
    }
  }
}
