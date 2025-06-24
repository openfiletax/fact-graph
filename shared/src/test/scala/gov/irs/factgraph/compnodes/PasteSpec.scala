package gov.irs.factgraph.compnodes

import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.fact.*
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result

class PasteSpec extends AnyFunSpec:
  describe("Paste") {
    it("concatenates strings") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "Paste",
          Seq(
            new CompNodeConfigElement(
              "String",
              Seq.empty,
              CommonOptionConfigTraits.value("Hello")
            ),
            new CompNodeConfigElement(
              "String",
              Seq.empty,
              CommonOptionConfigTraits.value("World")
            )
          )
        )
      )

      assert(node.get(0) == Result.Complete("Hello World"))
    }

    it("drops empty strings") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "Paste",
          Seq(
            new CompNodeConfigElement("String"),
            new CompNodeConfigElement(
              "String",
              Seq.empty,
              CommonOptionConfigTraits.value("Yo")
            ),
            new CompNodeConfigElement("String")
          )
        )
      )

      assert(node.get(0) == Result.Complete("Yo"))
    }

    it("accepts a custom separator") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "Paste",
          Seq(
            new CompNodeConfigElement(
              "String",
              Seq.empty,
              CommonOptionConfigTraits.value("Hello")
            ),
            new CompNodeConfigElement(
              "String",
              Seq.empty,
              CommonOptionConfigTraits.value("World")
            )
          ),
          CommonOptionConfigTraits.sep("!")
        )
      )

      assert(node.get(0) == Result.Complete("Hello!World"))
    }

    it("accepts an empty separator") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "Paste",
          Seq(
            new CompNodeConfigElement(
              "String",
              Seq.empty,
              CommonOptionConfigTraits.value("Com")
            ),
            new CompNodeConfigElement(
              "String",
              Seq.empty,
              CommonOptionConfigTraits.value("pound")
            )
          ),
          CommonOptionConfigTraits.sep("")
        )
      )

      assert(node.get(0) == Result.Complete("Compound"))
    }

    it("only accepts string inputs") {
      assertThrows[UnsupportedOperationException] {
        CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "Paste",
            Seq(
              new CompNodeConfigElement(
                "Int",
                Seq.empty,
                CommonOptionConfigTraits.value("101")
              ),
              new CompNodeConfigElement(
                "String",
                Seq.empty,
                CommonOptionConfigTraits.value("Dalmations")
              )
            )
          )
        )
      }
    }
  }
