package gov.irs.factgraph.limits

import gov.irs.factgraph.*
import gov.irs.factgraph.definitions.fact.*
import gov.irs.factgraph.types.Collection
import org.scalatest.funspec.AnyFunSpec

import java.util.{Date, UUID}

class MaxCollectionSizeSpec extends AnyFunSpec {
  private def generate(date: Date, a: Int, b: Int): UUID =
    new UUID(date.getTime, a.toLong << 31 + b.toLong)

  describe("MaxCollectionSize") {
    val limit = new LimitConfigTrait:
      override def operation: String = "MaxCollectionSize"
      override def level: LimitLevel = LimitLevel.Error
      override def node: CompNodeConfigTrait = new CompNodeConfigElement(
        "Int",
        Seq.empty,
        CommonOptionConfigTraits.value("100")
      )

    val dictionary = FactDictionary()
    FactDefinition.fromConfig(
      FactConfigElement(
        "/collection",
        Some(
          new WritableConfigElement("Collection", Seq.empty, Seq(limit))
        ),
        None,
        None
      )
    )(using dictionary)
    val graph = Graph(dictionary)

    val smallRange =
      Range.inclusive(1, 50).map(x => generate(new Date(), x * 2, x)).toVector
    val bigRange = smallRange ++ Range
      .inclusive(51, 102)
      .map(x => generate(new Date(), x * 2, x))
      .toVector
    it("doesn't affect collection under the max size") {
      for {
        result <- graph(Path("/collection"))
        fact <- result
      } fact.set(Collection(smallRange))
      var report = graph.save()
      assert(report._1 == true)
    }

    it("stops a collection too large from saving") {
      for {
        result <- graph(Path("/collection"))
        fact <- result
      } fact.set(Collection(bigRange))
      var report = graph.save()
      assert(report._1 == false)
      assert(report._2.head.limit == "100")
      assert(report._2.head.actual == "102")
    }

    it("only works with collections") {
      assertThrows[UnsupportedOperationException] {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/string",
            Some(
              new WritableConfigElement("String", Seq.empty, Seq(limit))
            ),
            None,
            None
          )
        )(using dictionary)
        val graph = Graph(dictionary)
        graph.set("/string", "test")
        graph.save()
      }
    }
  }

}
