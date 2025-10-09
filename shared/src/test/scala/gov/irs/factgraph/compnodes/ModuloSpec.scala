package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.*
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.Day
import gov.irs.factgraph.FactDictionary
import org.scalatest.funspec.AnyFunSpec

class ModuloSpec extends AnyFunSpec:
  describe("Modulo") {
    it("handles ints") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "Modulo",
          Seq(
            new CompNodeConfigElement(
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("10"),
            ),
            new CompNodeConfigElement(
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("3"),
            ),
          ),
        ),
      )

      assert(node.get(0) == Result.Complete(1))
    }

    it("throws errors when types don't match") {
      assertThrows[UnsupportedOperationException] {
        val node = CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "Modulo",
            Seq(
              new CompNodeConfigElement(
                "Dollar",
                Seq.empty,
                CommonOptionConfigTraits.value("10"),
              ),
              new CompNodeConfigElement(
                "Int",
                Seq.empty,
                CommonOptionConfigTraits.value("3"),
              ),
            ),
          ),
        )
      }
    }
  }
