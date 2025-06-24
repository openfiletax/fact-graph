package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{FactDefinition, FactDictionary, Factual, Graph, Path}
import gov.irs.factgraph.definitions.fact.*
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.*

import scala.collection.immutable.Seq

class EqualSpec extends AnyFunSpec:

  describe("Equal") {
    it("returns false if the inputs are different") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "Equal",
          Seq(
            new CompNodeConfigElement(
              "Left",
              Seq(
                new CompNodeConfigElement(
                  "Int",
                  Seq.empty,
                  CommonOptionConfigTraits.value("1")
                )
              )
            ),
            new CompNodeConfigElement(
              "Right",
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
      assert(node.get(0) == Result.Complete(false))
    }

    it("returns true if the inputs are the same") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "Equal",
          Seq(
            new CompNodeConfigElement(
              "Left",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("Test")
                )
              )
            ),
            new CompNodeConfigElement(
              "Right",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("Test")
                )
              )
            )
          )
        )
      )
      assert(node.get(0) == Result.Complete(true))
    }

    it("can compare an enum") {
      val dictionary = FactDictionary();
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "Equal",
          Seq(
            new CompNodeConfigElement(
              "Left",
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
                      (CommonOptionConfigTraits.VALUE, "A")
                    )
                  )
                )
              )
            )
          )
        )
      )(using given_Factual)(using dictionary)

      assert(node.get(0) == Result.Complete(true))
    }

    it("can compare an enum with a dependency") {
      val dictionary = FactDictionary();
      val factual = FactDefinition.fromConfig(
        FactConfigElement(
          "/test2",
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
                  CommonOptionConfigTraits.path("../test2")
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
                      (CommonOptionConfigTraits.VALUE, "A")
                    )
                  )
                )
              )
            )
          )
        )
      )(using factual)(using dictionary)

      assert(node.get(0) == Result.Incomplete)
    }

    it("can compare an enum with a dependency that has been set") {
      val testDictionary = FactDictionary();
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
      )(using testDictionary)
      val factual = FactDefinition.fromConfig(
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
      )(using testDictionary)

      val graph = Graph(testDictionary)
      val fact = graph(Path("/anotherName"))(0).get
      assert(fact.get(0) == Result.Incomplete)
      fact.value match
        case value: EnumNode => fact.set(Enum("D", "/options-path"))
      graph.save()

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
                  CommonOptionConfigTraits.path("../anotherName")
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
      )(using factual)(using testDictionary)
      assert(node.get(using fact)(0) == Result.Complete(true))
    }

    it("can compare an enum and fail too") {
      val dictionary = FactDictionary();
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "Equal",
          Seq(
            new CompNodeConfigElement(
              "Left",
              Seq(
                new CompNodeConfigElement(
                  "Enum",
                  Seq.empty,
                  CommonOptionConfigTraits.create(
                    Seq(
                      (CommonOptionConfigTraits.ENUM_OPTIONS_PATH, "test"),
                      (CommonOptionConfigTraits.VALUE, "A")
                    )
                  )
                )
              )
            ),
            new CompNodeConfigElement(
              "Right",
              Seq(
                new CompNodeConfigElement(
                  "Enum",
                  Seq.empty,
                  CommonOptionConfigTraits.create(
                    Seq(
                      (CommonOptionConfigTraits.ENUM_OPTIONS_PATH, "test"),
                      (CommonOptionConfigTraits.VALUE, "B")
                    )
                  )
                )
              )
            )
          )
        )
      )(using given_Factual)(using dictionary)

      assert(node.get(0) == Result.Complete(false))
    }

    it("requires both inputs to be of the same type") {
      assertThrows[UnsupportedOperationException] {
        CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "Equal",
            Seq(
              new CompNodeConfigElement(
                "Left",
                Seq(
                  new CompNodeConfigElement(
                    "Int",
                    Seq.empty,
                    CommonOptionConfigTraits.value("1")
                  )
                )
              ),
              new CompNodeConfigElement(
                "Right",
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
  }
