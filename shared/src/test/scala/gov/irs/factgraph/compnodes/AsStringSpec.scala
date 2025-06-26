package gov.irs.factgraph.compnodes

import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.fact.*
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result

class AsStringSpec extends AnyFunSpec:
  describe("AsString") {
    it("returns the value of its enum") {
      val node = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "AsString",
            Seq(
              new CompNodeConfigElement(
                "Enum",
                Seq.empty,
                CommonOptionConfigTraits.create(
                  Seq(
                    (
                      CommonOptionConfigTraits.ENUM_OPTIONS_PATH,
                      "/options-path"
                    ),
                    (CommonOptionConfigTraits.VALUE, "test")
                  )
                )
              )
            )
          )
        )
        .asInstanceOf[StringNode]

      assert(node.get(0) == Result.Complete("test"))
    }

    it("returns the value of its email address") {
      val node = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "AsString",
            Seq(
              new CompNodeConfigElement(
                "EmailAddress",
                Seq.empty,
                CommonOptionConfigTraits.value("example@example.com")
              )
            )
          )
        )
        .asInstanceOf[StringNode]

      assert(node.get(0) == Result.Complete("example@example.com"))
    }

    it("returns the value of a dollar node") {
      val node = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "AsString",
            Seq(
              new CompNodeConfigElement(
                "Dollar",
                Seq.empty,
                CommonOptionConfigTraits.value("50.20")
              )
            )
          )
        )
        .asInstanceOf[StringNode]

      assert(node.get(0) == Result.Complete("50.20"))
    }

    it("throws on a non-enum/email input") {
      assertThrows[UnsupportedOperationException] {
        CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "AsString",
            Seq(
              new CompNodeConfigElement(
                "Int",
                Seq.empty,
                CommonOptionConfigTraits.value("42")
              )
            )
          )
        )
      }
    }
  }
