package gov.irs.factgraph.compnodes

import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.fact.CompNodeConfigElement
import org.scalatest.funspec.AnyFunSpec

class DependencySpec extends AnyFunSpec:
  describe("Dependency") {
    it("throws an exception if the fact can't be found") {
      assertThrows[IllegalArgumentException] {
        CompNode.fromDerivedConfig(
          new CompNodeConfigElement("Dependency", Seq.empty, "nonExistentFact")
        )
      }
    }
  }
