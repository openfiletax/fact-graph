package gov.irs.factgraph.limits

import gov.irs.factgraph.{FactDefinition, FactDictionary, Graph, Path}
import gov.irs.factgraph.definitions.fact.{
  CommonOptionConfigTraits,
  CompNodeConfigElement,
  CompNodeConfigTrait,
  FactConfigElement,
  LimitConfigTrait,
  LimitLevel,
  WritableConfigElement
}
import gov.irs.factgraph.monads.Result
import org.scalatest.funspec.AnyFunSpec

class MatchSpec extends AnyFunSpec {

  describe("Match") {
    describe("operates on strings and") {
      it("allows a save if it there is no limit violation") {
        val limit = new LimitConfigTrait:
          override def operation: String = "Match"
          override def level: LimitLevel = LimitLevel.Error
          override def node: CompNodeConfigTrait = new CompNodeConfigElement(
            "String",
            Seq.empty,
            CommonOptionConfigTraits.value("a*")
          )
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("String", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set("aaaaaaaaaaaaaaaaaaaaaa")
        val saved = graph.save()
        assert(saved._1)
        assert(saved._2.length == 0)
        assert(fact.get(0) == Result.Complete("aaaaaaaaaaaaaaaaaaaaaa"))
      }

      it("allow a save if it there is a warning level violation") {
        val limit = new LimitConfigTrait:
          override def operation: String = "Match"
          override def level: LimitLevel = LimitLevel.Warn
          override def node: CompNodeConfigTrait = new CompNodeConfigElement(
            "String",
            Seq.empty,
            CommonOptionConfigTraits.value("a*")
          )
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("String", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set("abbbbbbbba")
        val saved = graph.save()
        assert(saved._1)
        assert(saved._2.length == 1)
        assert(saved._2.head.LimitLevel == LimitLevel.Warn)
        assert(saved._2.head.factPath == "/test")
        assert(saved._2.head.actual == "abbbbbbbba")
        assert(saved._2.head.limit == "a*")
        assert(saved._2.head.limitName == "Match")
        assert(fact.get(0) == Result.Complete("abbbbbbbba"))
      }

      it("and report an error if it violates the limit") {
        val limit = new LimitConfigTrait:
          override def operation: String = "Match"
          override def level: LimitLevel = LimitLevel.Error
          override def node: CompNodeConfigTrait = new CompNodeConfigElement(
            "String",
            Seq.empty,
            CommonOptionConfigTraits.value("a*")
          )
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("String", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set("bbbbbbbaaaaavvvvv")
        val saved = graph.save()
        assert(!saved._1)
        assert(saved._2.length == 1)
        assert(saved._2.head.LimitLevel == LimitLevel.Error)
        assert(saved._2.head.factPath == "/test")
        assert(saved._2.head.actual == "bbbbbbbaaaaavvvvv")
        assert(saved._2.head.limit == "a*")
        assert(saved._2.head.limitName == "Match")
        assert(fact.get(0) == Result.Incomplete)
      }
    }
  }
}
