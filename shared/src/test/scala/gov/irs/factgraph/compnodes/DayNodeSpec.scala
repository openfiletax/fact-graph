package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.*
import gov.irs.factgraph.{
  Expression,
  FactDefinition,
  FactDictionary,
  Graph,
  Path
}
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.Day
import org.scalatest.funspec.AnyFunSpec

class DayNodeSpec extends AnyFunSpec {
  describe("DayNode") {
    describe("$apply") {
      it("creates nodes from values") {
        val node = DayNode(Day("2022-01-01"))
        assert(node.get(0) == Result.Complete(Day("2022-01-01")))
      }
    }

    describe("$fromDerivedConfig") {
      it("parses config") {
        val node =
          CompNode
            .fromDerivedConfig(
              new CompNodeConfigElement(
                "Day",
                Seq.empty,
                CommonOptionConfigTraits.value("2022-01-01")
              )
            )
            .asInstanceOf[DayNode]
        assert(node.get(0) == Result.Complete(Day("2022-01-01")))
      }
    }

    describe(".switch") {
      it("can be used inside a switch statement") {
        val config = new CompNodeConfigElement(
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
                    new CompNodeConfigElement(
                      "Day",
                      Seq.empty,
                      CommonOptionConfigTraits.value("2022-01-01")
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
                    new CompNodeConfigElement(
                      "Day",
                      Seq.empty,
                      CommonOptionConfigTraits.value("2020-02-02")
                    )
                  )
                )
              )
            )
          )
        )
        val node = CompNode.fromDerivedConfig(config)
        assert(node.get(0) == Result.Complete(Day("2020-02-02")))
      }
    }

    describe(".dependency") {
      val dictionary = FactDictionary()
      FactDefinition.fromConfig(
        FactConfigElement(
          "/value",
          None,
          Some(
            CompNodeConfigElement(
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
          "/dependent",
          None,
          Some(
            new CompNodeConfigElement(
              "Dependency",
              Seq.empty,
              CommonOptionConfigTraits.path("../value")
            )
          ),
          None
        )
      )(using dictionary)

      val graph = Graph(dictionary)
      val dependent = graph(Path("/dependent"))(0).get

      it("can be depended on by another fact") {
        assert(dependent.value.isInstanceOf[DayNode])
        assert(dependent.get(0) == Result.Complete(Day("2022-01-01")))
      }
    }

    describe(".fromExpression") {
      it("can create a new node with an expression") {
        val node = DayNode(Expression.Constant(None))
        val newNode =
          node.fromExpression(Expression.Constant(Some(Day("2022-01-01"))))

        assert(newNode.get(0) == Result.Complete(Day("2022-01-01")))
      }
    }

    describe("$writablefromDerivedConfig") {
      it("can read and write a value") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Day")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(Day("2022-01-01"))
        graph.save()

        assert(fact.get(0) == Result.Complete(Day("2022-01-01")))
      }
    }

    describe("year") {
      it("can be gathered as an int") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Day")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        var incomplete = graph(Path("/test/year"))(0).get
        assert(incomplete.get(0) == Result.Incomplete)

        val fact = graph(Path("/test"))(0).get
        fact.set(Day("2022-01-01"))
        graph.save()
        var year = graph(Path("/test/year"))(0).get
        assert(year.get(0) == Result.Complete(2022))
      }

      it("can change values") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Day")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get
        fact.set(Day("2022-01-01"))
        graph.save()
        var year = graph(Path("/test/year"))(0).get
        assert(year.get(0) == Result.Complete(2022))

        fact.set(Day("2023-01-01"))
        graph.save()
        year = graph(Path("/test/year"))(0).get
        assert(year.get(0) == Result.Complete(2023))
      }
    }
    describe("month") {
      it("can be gathered as an int") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Day")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        var incomplete = graph(Path("/test/month"))(0).get
        assert(incomplete.get(0) == Result.Incomplete)

        val fact = graph(Path("/test"))(0).get
        fact.set(Day("2022-03-01"))
        graph.save()
        var year = graph(Path("/test/month"))(0).get
        assert(year.get(0) == Result.Complete(3))
      }

      it("can change values") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Day")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get
        fact.set(Day("2022-03-01"))
        graph.save()
        var year = graph(Path("/test/month"))(0).get
        assert(year.get(0) == Result.Complete(3))

        fact.set(Day("2023-05-01"))
        graph.save()
        year = graph(Path("/test/month"))(0).get
        assert(year.get(0) == Result.Complete(5))
      }
    }
    describe("day") {
      it("can be gathered as an int") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Day")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        var incomplete = graph(Path("/test/day"))(0).get
        assert(incomplete.get(0) == Result.Incomplete)

        val fact = graph(Path("/test"))(0).get
        fact.set(Day("2022-01-14"))
        graph.save()
        var year = graph(Path("/test/day"))(0).get
        assert(year.get(0) == Result.Complete(14))
      }

      it("can change values") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Day")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get
        fact.set(Day("2022-01-14"))
        graph.save()
        var year = graph(Path("/test/day"))(0).get
        assert(year.get(0) == Result.Complete(14))

        fact.set(Day("2023-01-21"))
        graph.save()
        year = graph(Path("/test/day"))(0).get
        assert(year.get(0) == Result.Complete(21))
      }
    }

  }
}
