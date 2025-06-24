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
import gov.irs.factgraph.types.{Day, Dollar, Rational}
import org.scalatest.funspec.AnyFunSpec

class MinSpec extends AnyFunSpec:
  describe("Min") {
    describe("can work with ints") {
      it("and allow a save if it there is no limit violation") {
        val limit = new LimitConfigTrait:
          override def operation: String = "Min"
          override def level: LimitLevel = LimitLevel.Error
          override def node: CompNodeConfigTrait = new CompNodeConfigElement(
            "Int",
            Seq.empty,
            CommonOptionConfigTraits.value("41")
          )
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Int", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(42)
        val saved = graph.save()
        assert(saved._1)
        assert(saved._2.length == 0)
        assert(fact.get(0) == Result.Complete(42))
      }

      it(
        "and allow an it to save if it is doesn't violate the limit set from another node"
      ) {
        val limit = new LimitConfigTrait:
          override def operation: String = "Min"
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
                CommonOptionConfigTraits.value("41")
              )
            ),
            None
          )
        )(using dictionary)

        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Int", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(42)
        val saved = graph.save()
        assert(saved._1)
        assert(saved._2.length == 0)
        assert(fact.get(0) == Result.Complete(42))
      }

      it("and allow a save if it there is a warning level violation") {
        val limit = new LimitConfigTrait:
          override def operation: String = "Min"
          override def level: LimitLevel = LimitLevel.Warn
          override def node: CompNodeConfigTrait = new CompNodeConfigElement(
            "Int",
            Seq.empty,
            CommonOptionConfigTraits.value("43")
          )
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Int", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(42)
        val saved = graph.save()
        assert(saved._1)
        assert(saved._2.length == 1)
        assert(saved._2.head.LimitLevel == LimitLevel.Warn)
        assert(saved._2.head.factPath == "/test")
        assert(saved._2.head.actual == "42")
        assert(saved._2.head.limit == "43")
        assert(saved._2.head.limitName == "Min")
        assert(fact.get(0) == Result.Complete(42))
      }

      it("and report an error if it violates the limit") {
        val limit = new LimitConfigTrait:
          override def operation: String = "Min"
          override def level: LimitLevel = LimitLevel.Error
          override def node: CompNodeConfigTrait = new CompNodeConfigElement(
            "Int",
            Seq.empty,
            CommonOptionConfigTraits.value("43")
          )
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Int", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(42)
        val saved = graph.save()
        assert(!saved._1)
        assert(saved._2.length == 1)
        assert(saved._2.head.LimitLevel == LimitLevel.Error)
        assert(saved._2.head.factPath == "/test")
        assert(saved._2.head.actual == "42")
        assert(saved._2.head.limit == "43")
        assert(saved._2.head.limitName == "Min")
        assert(fact.get(0) == Result.Incomplete)
      }

      it("and report an error if it violates the limit set from another node") {
        val limit = new LimitConfigTrait:
          override def operation: String = "Min"
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
                CommonOptionConfigTraits.value("43")
              )
            ),
            None
          )
        )(using dictionary)

        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Int", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(42)
        val saved = graph.save()
        assert(!saved._1)
        assert(saved._2.length == 1)
        assert(saved._2.head.LimitLevel == LimitLevel.Error)
        assert(saved._2.head.factPath == "/test")
        assert(saved._2.head.actual == "42")
        assert(saved._2.head.limit == "43")
        assert(saved._2.head.limitName == "Min")
        assert(fact.get(0) == Result.Incomplete)
      }
    }
    describe("can work with dollars") {
      it("and allow a save if it there is no limit violation") {
        val limit = new LimitConfigTrait:
          override def operation: String = "Min"
          override def level: LimitLevel = LimitLevel.Error
          override def node: CompNodeConfigTrait = new CompNodeConfigElement(
            "Dollar",
            Seq.empty,
            CommonOptionConfigTraits.value("41.25")
          )
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Dollar", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(Dollar("42.25"))
        val saved = graph.save()
        assert(saved._1)
        assert(saved._2.length == 0)
        assert(fact.get(0) == Result.Complete(Dollar("42.25")))
      }

      it(
        "and allow an it to save if it is doesn't violate the limit set from another node"
      ) {
        val limit = new LimitConfigTrait:
          override def operation: String = "Min"
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
                "Dollar",
                Seq.empty,
                CommonOptionConfigTraits.value("41.25")
              )
            ),
            None
          )
        )(using dictionary)

        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Dollar", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(Dollar("42.25"))
        val saved = graph.save()
        assert(saved._1)
        assert(saved._2.length == 0)
        assert(fact.get(0) == Result.Complete(Dollar("42.25")))
      }

      it("and allow a save if it there is a warning level violation") {
        val limit = new LimitConfigTrait:
          override def operation: String = "Min"
          override def level: LimitLevel = LimitLevel.Warn
          override def node: CompNodeConfigTrait = new CompNodeConfigElement(
            "Dollar",
            Seq.empty,
            CommonOptionConfigTraits.value("43.25")
          )
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Dollar", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(Dollar("42.99"))
        val saved = graph.save()
        assert(saved._1)
        assert(saved._2.length == 1)
        assert(saved._2.head.LimitLevel == LimitLevel.Warn)
        assert(saved._2.head.factPath == "/test")
        assert(saved._2.head.actual == "42.99")
        assert(saved._2.head.limit == "43.25")
        assert(saved._2.head.limitName == "Min")
        assert(fact.get(0) == Result.Complete(Dollar("42.99")))
      }

      it("and report an error if it violates the limit") {
        val limit = new LimitConfigTrait:
          override def operation: String = "Min"
          override def level: LimitLevel = LimitLevel.Error
          override def node: CompNodeConfigTrait = new CompNodeConfigElement(
            "Dollar",
            Seq.empty,
            CommonOptionConfigTraits.value("43.25")
          )
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Dollar", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(Dollar("42.99"))
        val saved = graph.save()
        assert(!saved._1)
        assert(saved._2.length == 1)
        assert(saved._2.head.LimitLevel == LimitLevel.Error)
        assert(saved._2.head.factPath == "/test")
        assert(saved._2.head.actual == "42.99")
        assert(saved._2.head.limit == "43.25")
        assert(saved._2.head.limitName == "Min")
        assert(fact.get(0) == Result.Incomplete)
      }

      it("and report an error if it violates the limit set from another node") {
        val limit = new LimitConfigTrait:
          override def operation: String = "Min"
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
                "Dollar",
                Seq.empty,
                CommonOptionConfigTraits.value("43.25")
              )
            ),
            None
          )
        )(using dictionary)

        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Dollar", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(Dollar("42.99"))
        val saved = graph.save()
        assert(!saved._1)
        assert(saved._2.length == 1)
        assert(saved._2.head.LimitLevel == LimitLevel.Error)
        assert(saved._2.head.factPath == "/test")
        assert(saved._2.head.actual == "42.99")
        assert(saved._2.head.limit == "43.25")
        assert(saved._2.head.limitName == "Min")
        assert(fact.get(0) == Result.Incomplete)
      }
    }
    describe("can work with rationals") {
      it("and allow a save if it there is no limit violation") {
        val limit = new LimitConfigTrait:
          override def operation: String = "Min"
          override def level: LimitLevel = LimitLevel.Error
          override def node: CompNodeConfigTrait = new CompNodeConfigElement(
            "Rational",
            Seq.empty,
            CommonOptionConfigTraits.value("1/3")
          )
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Rational", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(Rational("2/3"))
        val saved = graph.save()
        assert(saved._1)
        assert(saved._2.length == 0)
        assert(fact.get(0) == Result.Complete(Rational("2/3")))
      }

      it(
        "and allow an it to save if it is doesn't violate the limit set from another node"
      ) {
        val limit = new LimitConfigTrait:
          override def operation: String = "Min"

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
                "Rational",
                Seq.empty,
                CommonOptionConfigTraits.value("1/3")
              )
            ),
            None
          )
        )(using dictionary)

        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Rational", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(Rational("2/3"))
        val saved = graph.save()
        assert(saved._1)
        assert(saved._2.length == 0)
        assert(fact.get(0) == Result.Complete(Rational("2/3")))
      }

      it("and allow a save if it there is a warning level violation") {
        val limit = new LimitConfigTrait:
          override def operation: String = "Min"

          override def level: LimitLevel = LimitLevel.Warn

          override def node: CompNodeConfigTrait = new CompNodeConfigElement(
            "Rational",
            Seq.empty,
            CommonOptionConfigTraits.value("5/6")
          )
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Rational", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(Rational("2/3"))
        val saved = graph.save()
        assert(saved._1)
        assert(saved._2.length == 1)
        assert(saved._2.head.LimitLevel == LimitLevel.Warn)
        assert(saved._2.head.factPath == "/test")
        assert(saved._2.head.actual == "2/3")
        assert(saved._2.head.limit == "5/6")
        assert(saved._2.head.limitName == "Min")
        assert(fact.get(0) == Result.Complete(Rational("2/3")))
      }

      it("and report an error if it violates the limit") {
        val limit = new LimitConfigTrait:
          override def operation: String = "Min"

          override def level: LimitLevel = LimitLevel.Error

          override def node: CompNodeConfigTrait = new CompNodeConfigElement(
            "Rational",
            Seq.empty,
            CommonOptionConfigTraits.value("5/6")
          )
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Rational", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(Rational("2/3"))
        val saved = graph.save()
        assert(!saved._1)
        assert(saved._2.length == 1)
        assert(saved._2.head.LimitLevel == LimitLevel.Error)
        assert(saved._2.head.factPath == "/test")
        assert(saved._2.head.actual == "2/3")
        assert(saved._2.head.limit == "5/6")
        assert(saved._2.head.limitName == "Min")
        assert(fact.get(0) == Result.Incomplete)
      }

      it("and report an error if it violates the limit set from another node") {
        val limit = new LimitConfigTrait:
          override def operation: String = "Min"

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
                "Rational",
                Seq.empty,
                CommonOptionConfigTraits.value("5/6")
              )
            ),
            None
          )
        )(using dictionary)

        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Rational", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(Rational("2/3"))
        val saved = graph.save()
        assert(!saved._1)
        assert(saved._2.length == 1)
        assert(saved._2.head.LimitLevel == LimitLevel.Error)
        assert(saved._2.head.factPath == "/test")
        assert(saved._2.head.actual == "2/3")
        assert(saved._2.head.limit == "5/6")
        assert(saved._2.head.limitName == "Min")
        assert(fact.get(0) == Result.Incomplete)
      }
    }
    describe("can work with days") {
      it("and allow a save if it there is no limit violation") {
        val limit = new LimitConfigTrait:
          override def operation: String = "Min"
          override def level: LimitLevel = LimitLevel.Error
          override def node: CompNodeConfigTrait = new CompNodeConfigElement(
            "Day",
            Seq.empty,
            CommonOptionConfigTraits.value("2022-01-01")
          )
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Day", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(Day("2023-01-01"))
        val saved = graph.save()
        assert(saved._1)
        assert(saved._2.length == 0)
        assert(fact.get(0) == Result.Complete(Day("2023-01-01")))
      }

      it(
        "and allow an it to save if it is doesn't violate the limit set from another node"
      ) {
        val limit = new LimitConfigTrait:
          override def operation: String = "Min"

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
                "Day",
                Seq.empty,
                CommonOptionConfigTraits.value("2022-01-01")
              )
            ),
            None
          )
        )(using dictionary)

        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Day", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(Day("2023-01-01"))
        val saved = graph.save()
        assert(saved._1)
        assert(saved._2.length == 0)
        assert(fact.get(0) == Result.Complete(Day("2023-01-01")))
      }

      it("and allow a save if it there is a warning level violation") {
        val limit = new LimitConfigTrait:
          override def operation: String = "Min"

          override def level: LimitLevel = LimitLevel.Warn

          override def node: CompNodeConfigTrait = new CompNodeConfigElement(
            "Day",
            Seq.empty,
            CommonOptionConfigTraits.value("2022-01-01")
          )
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Day", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(Day("2021-01-01"))
        val saved = graph.save()
        assert(saved._1)
        assert(saved._2.length == 1)
        assert(saved._2.head.LimitLevel == LimitLevel.Warn)
        assert(saved._2.head.factPath == "/test")
        assert(saved._2.head.actual == "2021-01-01")
        assert(saved._2.head.limit == "2022-01-01")
        assert(saved._2.head.limitName == "Min")
        assert(fact.get(0) == Result.Complete(Day("2021-01-01")))
      }

      it("and report an error if it violates the limit") {
        val limit = new LimitConfigTrait:
          override def operation: String = "Min"

          override def level: LimitLevel = LimitLevel.Error

          override def node: CompNodeConfigTrait = new CompNodeConfigElement(
            "Day",
            Seq.empty,
            CommonOptionConfigTraits.value("2022-01-01")
          )
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Day", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(Day("2021-01-01"))
        val saved = graph.save()
        assert(!saved._1)
        assert(saved._2.length == 1)
        assert(saved._2.head.LimitLevel == LimitLevel.Error)
        assert(saved._2.head.factPath == "/test")
        assert(saved._2.head.actual == "2021-01-01")
        assert(saved._2.head.limit == "2022-01-01")
        assert(saved._2.head.limitName == "Min")
        assert(fact.get(0) == Result.Incomplete)
      }

      it("and report an error if it violates the limit set from another node") {
        val limit = new LimitConfigTrait:
          override def operation: String = "Min"

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
                "Day",
                Seq.empty,
                CommonOptionConfigTraits.value("2022-01-01")
              )
            ),
            None
          )
        )(using dictionary)

        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Day", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(Day("2021-01-01"))
        val saved = graph.save()
        assert(!saved._1)
        assert(saved._2.length == 1)
        assert(saved._2.head.LimitLevel == LimitLevel.Error)
        assert(saved._2.head.factPath == "/test")
        assert(saved._2.head.actual == "2021-01-01")
        assert(saved._2.head.limit == "2022-01-01")
        assert(saved._2.head.limitName == "Min")
        assert(fact.get(0) == Result.Incomplete)
      }
    }
  }
