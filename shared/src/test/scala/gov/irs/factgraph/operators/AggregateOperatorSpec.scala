package gov.irs.factgraph.operators

import org.scalatest.funspec.AnyFunSpec

import gov.irs.factgraph.*
import gov.irs.factgraph.types.*
import gov.irs.factgraph.definitions.fact.{
  CompNodeConfigElement,
  FactConfigElement,
  WritableConfigElement
}

import java.util.UUID

class AggregateOperatorSpec extends AnyFunSpec:
  describe("AggregateOperator") {
    val dictionary = FactDictionary()
    FactDefinition.fromConfig(
      FactConfigElement(
        "/test",
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

    val graph = Graph(dictionary)

    val uuid1: UUID = UUID.fromString("59a3c760-2fac-45e2-a6cd-0792c4aef83e")
    val uuid2: UUID = UUID.fromString("41042a1e-a2a2-459d-9f39-ccaac5612014")

    for {
      result <- graph(Path("/collection"))
      fact <- result
    } fact.set(Collection(Vector(uuid1, uuid2)))

    graph.save()

    for {
      result <- graph(Path(s"/collection/#$uuid1/int"))
      fact <- result
    } fact.set(1)

    for {
      result <- graph(Path(s"/collection/#$uuid2/int"))
      fact <- result
    } fact.set(2)

    graph.save()

    val fact = graph(Path("/test"))(0).get

    describe(".explain") {
      it("coalesces a vector of explanations as its inclusive children") {
        assert(
          fact.explain(0) == Explanation.Operation(
            List(
              List(
                Explanation.Dependency(
                  true,
                  Path("/test"),
                  Path("/collection/*/int"),
                  List(
                    List(
                      Explanation.Writable(
                        true,
                        Path(s"/collection/#$uuid1/int")
                      )
                    )
                  )
                ),
                Explanation.Dependency(
                  true,
                  Path("/test"),
                  Path("/collection/*/int"),
                  List(
                    List(
                      Explanation.Writable(
                        true,
                        Path(s"/collection/#$uuid2/int")
                      )
                    )
                  )
                )
              )
            )
          )
        )
      }
    }
  }
