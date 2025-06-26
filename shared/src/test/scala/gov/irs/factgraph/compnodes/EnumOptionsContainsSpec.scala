package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{FactDefinition, FactDictionary, Factual, Graph, Path}
import gov.irs.factgraph.definitions.fact.*
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.*

import scala.collection.immutable.Seq

class EnumOptionsContains extends AnyFunSpec:
  describe("EnumOptionsContains") {
    it(
      "has enum options as the first parameter and finds element"
    ) {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "EnumOptionsContains",
          Seq(
            new CompNodeConfigElement(
              "Options",
              Seq(
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
                    )
                  ),
                  Seq.empty
                )
              )
            ),
            new CompNodeConfigElement(
              "Value",
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
      )
      assert(node.get(0) == Result.Complete(true))
    }

    it(
      "has enum options as the first parameter and doesn't find element"
    ) {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "EnumOptionsContains",
          Seq(
            new CompNodeConfigElement(
              "Options",
              Seq(
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
                    )
                  ),
                  Seq.empty
                )
              )
            ),
            new CompNodeConfigElement(
              "Value",
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
                      (CommonOptionConfigTraits.VALUE, "C")
                    )
                  )
                )
              )
            )
          )
        )
      )
      assert(node.get(0) == Result.Complete(false))
    }

    it(
      "has MultiEnum options as the first parameter and finds element"
    ) {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "EnumOptionsContains",
          Seq(
            new CompNodeConfigElement(
              "Options",
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
                      (CommonOptionConfigTraits.VALUE, "A,B")
                    )
                  )
                )
              )
            ),
            new CompNodeConfigElement(
              "Value",
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
      assert(node.get(0) == Result.Complete(true))
    }

    it(
      "has MultiEnum options as the first parameter and doesn't find element"
    ) {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "EnumOptionsContains",
          Seq(
            new CompNodeConfigElement(
              "Options",
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
                      (CommonOptionConfigTraits.VALUE, "A,B")
                    )
                  )
                )
              )
            ),
            new CompNodeConfigElement(
              "Value",
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
                      (CommonOptionConfigTraits.VALUE, "C")
                    )
                  )
                )
              )
            )
          )
        )
      )
      assert(node.get(0) == Result.Complete(false))
    }

  }
