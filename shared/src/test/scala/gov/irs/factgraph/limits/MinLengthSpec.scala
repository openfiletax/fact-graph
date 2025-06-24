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

class MinLengthSpec extends AnyFunSpec {
  describe("MinLength") {
    describe("can work with strings") {
      it("and allow a save if it there is no limit violation") {
        val limit = new LimitConfigTrait:
          override def operation: String = "MinLength"
          override def level: LimitLevel = LimitLevel.Error
          override def node: CompNodeConfigTrait = new CompNodeConfigElement(
            "Int",
            Seq.empty,
            CommonOptionConfigTraits.value("3")
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

        fact.set("ABC")
        val saved = graph.save()
        assert(saved._1)
        assert(saved._2.length == 0)
        assert(fact.get(0) == Result.Complete("ABC"))
      }

      it(
        "and allow an it to save if it is doesn't violate the limit set from another node"
      ) {
        val limit = new LimitConfigTrait:
          override def operation: String = "MinLength"

          override def level: LimitLevel = LimitLevel.Error

          override def node: CompNodeConfigTrait = new CompNodeConfigElement(
            "Dependency",
            Seq.empty,
            CommonOptionConfigTraits.path("/constraint")
          )
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/constraint",
            None,
            Some(
              new CompNodeConfigElement(
                "Int",
                Seq.empty,
                CommonOptionConfigTraits.value("3")
              )
            ),
            None
          )
        )(using dictionary)

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

        fact.set("ABC")
        val saved = graph.save()
        assert(saved._1)
        assert(saved._2.length == 0)
        assert(fact.get(0) == Result.Complete("ABC"))
      }

      it("and allow a save if it there is a warning level violation") {
        val limit = new LimitConfigTrait:
          override def operation: String = "MinLength"
          override def level: LimitLevel = LimitLevel.Warn
          override def node: CompNodeConfigTrait = new CompNodeConfigElement(
            "Int",
            Seq.empty,
            CommonOptionConfigTraits.value("3")
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

        fact.set("A")
        val saved = graph.save()
        assert(saved._1)
        assert(saved._2.length == 1)
        assert(saved._2.head.LimitLevel == LimitLevel.Warn)
        assert(saved._2.head.factPath == "/test")
        assert(saved._2.head.actual == "1")
        assert(saved._2.head.limit == "3")
        assert(saved._2.head.limitName == "MinLength")
        assert(fact.get(0) == Result.Complete("A"))
      }

      it("and report an error if it violates the limit") {
        val limit = new LimitConfigTrait:
          override def operation: String = "MinLength"

          override def level: LimitLevel = LimitLevel.Error

          override def node: CompNodeConfigTrait = new CompNodeConfigElement(
            "Int",
            Seq.empty,
            CommonOptionConfigTraits.value("3")
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

        fact.set("A")
        val saved = graph.save()
        assert(!saved._1)
        assert(saved._2.length == 1)
        assert(saved._2.head.LimitLevel == LimitLevel.Error)
        assert(saved._2.head.factPath == "/test")
        assert(saved._2.head.actual == "1")
        assert(saved._2.head.limit == "3")
        assert(saved._2.head.limitName == "MinLength")
        assert(fact.get(0) == Result.Incomplete)
      }

      it("and report an error if it violates the limit set from another node") {
        val limit = new LimitConfigTrait:
          override def operation: String = "MinLength"
          override def level: LimitLevel = LimitLevel.Error
          override def node: CompNodeConfigTrait = new CompNodeConfigElement(
            "Dependency",
            Seq.empty,
            CommonOptionConfigTraits.path("/constraint")
          )
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/constraint",
            None,
            Some(
              new CompNodeConfigElement(
                "Int",
                Seq.empty,
                CommonOptionConfigTraits.value("3")
              )
            ),
            None
          )
        )(using dictionary)

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

        fact.set("A")
        val saved = graph.save()
        assert(!saved._1)
        assert(saved._2.length == 1)
        assert(saved._2.head.LimitLevel == LimitLevel.Error)
        assert(saved._2.head.factPath == "/test")
        assert(saved._2.head.actual == "1")
        assert(saved._2.head.limit == "3")
        assert(saved._2.head.limitName == "MinLength")
        assert(fact.get(0) == Result.Incomplete)
      }

      it("only works on strings") {
        assertThrows[UnsupportedOperationException] {
          val limit = new LimitConfigTrait:
            override def operation: String = "MinLength"

            override def level: LimitLevel = LimitLevel.Warn

            override def node: CompNodeConfigTrait = new CompNodeConfigElement(
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("1")
            )
          val dictionary = FactDictionary()
          FactDefinition.fromConfig(
            FactConfigElement(
              "/int",
              Some(
                new WritableConfigElement("Int", Seq.empty, Seq(limit))
              ),
              None,
              None
            )
          )(using dictionary)
          val graph = Graph(dictionary)
          graph.set("/int", 10)
          graph.save()
        }
      }
    }
  }
}
