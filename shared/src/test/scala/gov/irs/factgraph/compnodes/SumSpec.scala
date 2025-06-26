package gov.irs.factgraph.compnodes

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.*
import gov.irs.factgraph.*
import gov.irs.factgraph.definitions.fact.{
  CompNodeConfigElement,
  FactConfigElement,
  WritableConfigElement
}

import java.util.UUID

class SumSpec extends AnyFunSpec:
  describe("CollectionSum") {
    val dictionary = FactDictionary()
    FactDefinition.fromConfig(
      FactConfigElement(
        "/intTest",
        None,
        Some(
          new CompNodeConfigElement(
            "CollectionSum",
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
            "CollectionSum",
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
            "CollectionSum",
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
        "/collection/*/string",
        Some(
          new WritableConfigElement("String")
        ),
        None,
        None
      )
    )(using dictionary)

    val graph = Graph(dictionary)

    val uuid1: UUID = UUID.fromString("59a3c760-2fac-45e2-a6cd-0792c4aef83e")
    val uuid2: UUID = UUID.fromString("41042a1e-a2a2-459d-9f39-ccaac5612014")

    for {
      result <- graph(Path("/collection"))
      fact <- result
    } fact.set(Collection(Vector(uuid1, uuid2)))

    graph.save()

    it("sums Ints") {
      val fact = graph(Path("/intTest"))(0).get

      assert(fact.get(0) == Result.Incomplete)

      for {
        result <- graph(Path(s"/collection/#$uuid1/int"))
        fact <- result
      } fact.set(1)

      for {
        result <- graph(Path(s"/collection/#$uuid2/int"))
        fact <- result
      } fact.set(2)

      graph.save()

      assert(fact.get(0) == Result.Complete(3))
    }

    it("sums Rationals") {
      val fact = graph(Path("/rationalTest"))(0).get

      assert(fact.get(0) == Result.Incomplete)

      for {
        result <- graph(Path(s"/collection/#$uuid1/rational"))
        fact <- result
      } fact.set(Rational("1/2"))

      for {
        result <- graph(Path(s"/collection/#$uuid2/rational"))
        fact <- result
      } fact.set(Rational("1/3"))

      graph.save()

      assert(fact.get(0) == Result.Complete(Rational("5/6")))
    }

    it("sums Dollars") {
      val fact = graph(Path("/dollarTest"))(0).get

      assert(fact.get(0) == Result.Incomplete)

      for {
        result <- graph(Path(s"/collection/#$uuid1/dollar"))
        fact <- result
      } fact.set(Dollar("1.23"))

      for {
        result <- graph(Path(s"/collection/#$uuid2/dollar"))
        fact <- result
      } fact.set(Dollar("4.56"))

      graph.save()

      assert(fact.get(0) == Result.Complete(Dollar("5.79")))
    }

    it("throws an error if asked to sum non-numeric nodes") {
      assertThrows[UnsupportedOperationException] {
        val definition = FactDefinition.fromConfig(
          FactConfigElement(
            "/stringTest",
            None,
            Some(
              new CompNodeConfigElement(
                "CollectionSum",
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
        )(using dictionary)

        definition.meta
      }
    }
  }
