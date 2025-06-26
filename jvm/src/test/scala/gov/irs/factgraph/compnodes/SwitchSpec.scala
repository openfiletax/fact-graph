package gov.irs.factgraph.compnodes

import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.fact.{
  CommonOptionConfigTraits,
  CompNodeConfigElement
}
import gov.irs.factgraph.monads.Result
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.funsuite.AnyFunSuite
import gov.irs.factgraph.definitions.fact.FactConfigElement
import gov.irs.factgraph.FactDefinition
import gov.irs.factgraph.Graph
import gov.irs.factgraph.Path

class SwitchSpec extends AnyFunSpec:
  describe("Switch") {
    it("cannot be empty") {
      assertThrows[IllegalArgumentException] {
        CompNode.fromDerivedConfig(new CompNodeConfigElement("Switch"))
      }
    }

    it("must contain a boolean condition") {
      assertThrows[UnsupportedOperationException] {
        CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "Switch",
            Seq(
              new CompNodeConfigElement(
                "Case",
                Seq(
                  new CompNodeConfigElement(
                    "When",
                    Seq(
                      CompNodeConfigElement(
                        "String",
                        Seq.empty,
                        CommonOptionConfigTraits.value("Hello")
                      )
                    )
                  ),
                  new CompNodeConfigElement(
                    "Then",
                    Seq(
                      CompNodeConfigElement(
                        "String",
                        Seq.empty,
                        CommonOptionConfigTraits.value("World")
                      )
                    )
                  )
                )
              )
            )
          )
        )
      }
    }

    it("can switch on a dependency") {
      val dictionary = FactDictionary.apply()
      val predicateConfig = FactConfigElement(
        "/predicate",
        None,
        Some(new CompNodeConfigElement("True")),
        None
      )
      FactDefinition.fromConfig(predicateConfig)(using dictionary)

      val switchConfig = FactConfigElement(
        "/test-switch",
        None,
        Some(
          new CompNodeConfigElement(
            "Switch",
            Seq(
              new CompNodeConfigElement(
                "Case",
                Seq(
                  new CompNodeConfigElement(
                    "When",
                    Seq(
                      new CompNodeConfigElement(
                        "Dependency",
                        Seq.empty,
                        CommonOptionConfigTraits.path("/predicate")
                      )
                    )
                  ),
                  new CompNodeConfigElement(
                    "Then",
                    Seq(
                      CompNodeConfigElement(
                        "String",
                        Seq.empty,
                        CommonOptionConfigTraits.value("World")
                      )
                    )
                  )
                )
              )
            )
          )
        ),
        None
      )

      FactDefinition.fromConfig(switchConfig)(using dictionary)
      val graph = Graph(dictionary)

      assert(graph.get("/predicate") == Result.Complete(true))
      val thens = graph.get("/test-switch")
      assert(thens == Result.Complete("World"))
    }

    it("all results must be of the same type") {
      assertThrows[UnsupportedOperationException] {
        CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
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
                      CompNodeConfigElement(
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
                      CompNodeConfigElement(
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
        )
      }
    }
  }
