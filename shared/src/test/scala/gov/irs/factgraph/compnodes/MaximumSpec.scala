package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{FactDefinition, FactDictionary, Graph, Path}
import gov.irs.factgraph.definitions.fact.{
  CompNodeConfigElement,
  FactConfigElement,
  WritableConfigElement
}
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.*
import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec
import java.util.UUID

class MaximumSpec extends AnyFunSpec with BeforeAndAfter:
  val uuid1: UUID = UUID.fromString("59a3c760-2fac-45e2-a6cd-0792c4aef83e")
  val uuid2: UUID = UUID.fromString("41042a1e-a2a2-459d-9f39-ccaac5612014")
  var factDictionary: FactDictionary = FactDictionary()
  var factGraph: Graph = Graph(factDictionary)

  before {
    val dictionary = FactDictionary()
    FactDefinition.fromConfig(
      FactConfigElement(
        "/intTest",
        None,
        Some(
          new CompNodeConfigElement(
            "Maximum",
            Seq(
              new CompNodeConfigElement(
                "Dependency",
                Seq.empty,
                "/collection/*/int"
              )
            )
          )
        ),
        None
      )
    )(using dictionary)

    FactDefinition.fromConfig(
      FactConfigElement(
        "/rationalTest",
        None,
        Some(
          new CompNodeConfigElement(
            "Maximum",
            Seq(
              new CompNodeConfigElement(
                "Dependency",
                Seq.empty,
                "/collection/*/rational"
              )
            )
          )
        ),
        None
      )
    )(using dictionary)

    FactDefinition.fromConfig(
      FactConfigElement(
        "/dollarTest",
        None,
        Some(
          new CompNodeConfigElement(
            "Maximum",
            Seq(
              new CompNodeConfigElement(
                "Dependency",
                Seq.empty,
                "/collection/*/dollar"
              )
            )
          )
        ),
        None
      )
    )(using dictionary)

    FactDefinition.fromConfig(
      FactConfigElement(
        "/dayTest",
        None,
        Some(
          new CompNodeConfigElement(
            "Maximum",
            Seq(
              new CompNodeConfigElement(
                "Dependency",
                Seq.empty,
                "/collection/*/day"
              )
            )
          )
        ),
        None
      )
    )(using dictionary)

    FactDefinition.fromConfig(
      FactConfigElement(
        "/collection",
        Some(
          new WritableConfigElement("Collection")
        ),
        None,
        None
      )
    )(using dictionary)

    FactDefinition.fromConfig(
      FactConfigElement(
        "/collection/*/int",
        Some(
          new WritableConfigElement("Int")
        ),
        None,
        None
      )
    )(using dictionary)

    FactDefinition.fromConfig(
      FactConfigElement(
        "/collection/*/rational",
        Some(
          new WritableConfigElement("Rational")
        ),
        None,
        None
      )
    )(using dictionary)

    FactDefinition.fromConfig(
      FactConfigElement(
        "/collection/*/dollar",
        Some(
          new WritableConfigElement("Dollar")
        ),
        None,
        None
      )
    )(using dictionary)

    FactDefinition.fromConfig(
      FactConfigElement(
        "/collection/*/day",
        Some(
          new WritableConfigElement("Day")
        ),
        None,
        None
      )
    )(using dictionary)

    FactDefinition.fromConfig(
      FactConfigElement(
        "/collection/*/string",
        Some(
          new WritableConfigElement("String")
        ),
        None,
        None
      )
    )(using dictionary)

    val graph = Graph(dictionary)

    for {
      result <- graph(Path("/collection"))
      fact <- result
    } fact.set(Collection(Vector(uuid1, uuid2)))

    graph.save()
    factDictionary = dictionary
    factGraph = graph
  }

  describe("Maximum") {
    describe("can use integers") {
      it("finds the maximum in a collection") {
        val fact = factGraph(Path("/intTest"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        for {
          result <- factGraph(Path(s"/collection/#$uuid1/int"))
          fact <- result
        } fact.set(10)

        for {
          result <- factGraph(Path(s"/collection/#$uuid2/int"))
          fact <- result
        } fact.set(20)

        factGraph.save()

        assert(fact.get(0) == Result.Complete(20))
      }

      it(
        "finds the maximum in a collection and returns incomplete if not all items are complete"
      ) {
        val fact = factGraph(Path("/intTest"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        for {
          result <- factGraph(Path(s"/collection/#$uuid1/int"))
          fact <- result
        } fact.set(3)

        factGraph.save()

        assert(fact.get(0) == Result.Incomplete)
      }
    }
    describe("can use dollars") {
      it("finds the maximum in a collection") {
        val fact = factGraph(Path("/dollarTest"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        for {
          result <- factGraph(Path(s"/collection/#$uuid1/dollar"))
          fact <- result
        } fact.set(Dollar(30))

        for {
          result <- factGraph(Path(s"/collection/#$uuid2/dollar"))
          fact <- result
        } fact.set(Dollar(40))

        factGraph.save()

        assert(fact.get(0) == Result.Complete(Dollar(40)))
      }

      it(
        "finds the maximum in a collection and returns incomplete if not all items are complete"
      ) {
        val fact = factGraph(Path("/dollarTest"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        for {
          result <- factGraph(Path(s"/collection/#$uuid1/dollar"))
          fact <- result
        } fact.set(Dollar(25))

        factGraph.save()

        assert(fact.get(0) == Result.Incomplete)
      }
    }

    describe("can use day") {
      it("finds the maximum in a collection") {
        val fact = factGraph(Path("/dayTest"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        for {
          result <- factGraph(Path(s"/collection/#$uuid1/day"))
          fact <- result
        } fact.set(Day("2022-03-03"))

        for {
          result <- factGraph(Path(s"/collection/#$uuid2/day"))
          fact <- result
        } fact.set(Day("2022-03-02"))

        factGraph.save()

        assert(fact.get(0) == Result.Complete(Day("2022-03-03")))
      }

      it(
        "finds the maximum in a collection and returns incomplete if not all items are complete"
      ) {
        val fact = factGraph(Path("/dayTest"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        for {
          result <- factGraph(Path(s"/collection/#$uuid1/day"))
          fact <- result
        } fact.set(Day("2022-03-02"))

        factGraph.save()

        assert(fact.get(0) == Result.Incomplete)
      }
    }

    describe("can use rational") {
      it("finds the maximum in a collection") {
        val fact = factGraph(Path("/rationalTest"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        for {
          result <- factGraph(Path(s"/collection/#$uuid1/rational"))
          fact <- result
        } fact.set(Rational("1/3"))

        for {
          result <- factGraph(Path(s"/collection/#$uuid2/rational"))
          fact <- result
        } fact.set(Rational("2/3"))

        factGraph.save()

        assert(fact.get(0) == Result.Complete(Rational("2/3")))
      }

      it(
        "finds the maximum in a collection and returns incomplete if not all items are complete"
      ) {
        val fact = factGraph(Path("/rationalTest"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        for {
          result <- factGraph(Path(s"/collection/#$uuid1/rational"))
          fact <- result
        } fact.set(Rational("2/3"))

        factGraph.save()

        assert(fact.get(0) == Result.Incomplete)
      }
    }

    describe("it can't be used with strings") {
      it("will throw an error") {
        assertThrows[UnsupportedOperationException] {
          val definition = FactDefinition.fromConfig(
            FactConfigElement(
              "/stringTest",
              None,
              Some(
                new CompNodeConfigElement(
                  "Maximum",
                  Seq(
                    new CompNodeConfigElement(
                      "Dependency",
                      Seq.empty,
                      "/collection/*/string"
                    )
                  )
                )
              ),
              None
            )
          )(using factDictionary)

          definition.meta
        }
      }
    }
  }
