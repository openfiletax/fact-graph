package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.*
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.*
import gov.irs.factgraph.FactDictionary
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.funsuite.AnyFunSuite

class RoundSpec extends AnyFunSuite:
  test("from config") {
    val node = CompNode.fromDerivedConfig(
      new CompNodeConfigElement(
        "Round",
        Seq(
          new CompNodeConfigElement(
            "Dollar",
            Seq.empty,
            CommonOptionConfigTraits.value("1.50"),
          ),
        ),
      ),
    )

    assert(node.get(0) == Result.Complete(Dollar("2.00")))
  }

  test("error cases") {
    assertThrows[UnsupportedOperationException] {
      CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "Round",
          Seq(
            new CompNodeConfigElement(
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("123"),
            ),
          ),
        ),
      )
    }
  }
