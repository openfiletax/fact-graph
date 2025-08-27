package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.CommonOptionConfigTraits
import gov.irs.factgraph.definitions.fact.CompNodeConfigElement
import gov.irs.factgraph.definitions.fact.FactConfigElement
import gov.irs.factgraph.definitions.fact.WritableConfigElement
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.FactDefinition
import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.Graph
import org.scalatest.funspec.AnyFunSpec

class OverrideSpec extends AnyFunSpec:
  describe("Override") {
    it("cannot be created from config") {
      assertThrows[UnsupportedOperationException] {
        CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "Override",
          ),
        )
      }
    }
    describe("can be created from a fact dictionary") {
      it("maintains default value if condition is true") {
        val dictionary = FactDictionary()
        val definition = FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("String"),
            ),
            None,
            None,
            Some(
              new CompNodeConfigElement("True"),
            ),
            Some(
              CompNodeConfigElement(
                "String",
                Seq.empty,
                CommonOptionConfigTraits.value("default"),
              ),
            ),
          ),
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph.get("/test")

        assert(fact == Result.Complete("default"))

        graph.set("/test", "Hello world")
        graph.save()

        assert(graph.get("/test") == Result.Complete("default"))
      }

      it("maintains ignores the default value if condition is false") {
        val dictionary = FactDictionary()
        val definition = FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("String"),
            ),
            None,
            None,
            Some(
              new CompNodeConfigElement("False"),
            ),
            Some(
              CompNodeConfigElement(
                "String",
                Seq.empty,
                CommonOptionConfigTraits.value("default"),
              ),
            ),
          ),
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph.get("/test")

        assert(fact == Result.Incomplete)

        graph.set("/test", "Hello world")
        graph.save()

        assert(graph.get("/test") == Result.Complete("Hello world"))
      }
    }
  }
