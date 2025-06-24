package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.*
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.{FactDictionary, FactDefinition, Graph, Path}
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.Day
import java.time.LocalDate
import java.time.ZoneId

class TodayNodeSpec extends AnyFunSpec {
  describe("TodayNode") {
    it("returns the current date based on the offset passed in") {
      val dictionary = FactDictionary()
      FactDefinition.fromConfig(
        FactConfigElement(
          "/test",
          None,
          Some(
            new CompNodeConfigElement(
              "Today",
              Seq(
                new CompNodeConfigElement(
                  "Int",
                  Seq.empty,
                  CommonOptionConfigTraits.value("-5")
                )
              )
            )
          ),
          None
        )
      )(using dictionary)
      val graph = Graph(dictionary)
      val today =
        LocalDate.now(ZoneId.ofOffset("UTC", java.time.ZoneOffset.ofHours(-5)))
      val fact = graph(Path("/test"))(0).get
      assert(fact.get(0) == Result.Complete(Day(today)))
    }
  }

}
