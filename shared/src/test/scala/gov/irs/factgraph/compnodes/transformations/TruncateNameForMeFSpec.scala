package gov.irs.factgraph.compnodes

import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.fact.*
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result

class MeFName extends AnyFunSpec:
  describe("TruncateNameForMeF") {
    it("doesn't modify short names at all") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "TruncateNameForMeF",
          Seq(
            new CompNodeConfigElement(
              "FirstName",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("Short")
                )
              )
            ),
            new CompNodeConfigElement(
              "MiddleInitial",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("T")
                )
              )
            ),
            new CompNodeConfigElement(
              "LastName",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("Name")
                )
              )
            ),
            new CompNodeConfigElement(
              "Suffix",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("II")
                )
              )
            )
          )
        )
      )
      assert(node.get(0) == Result.Complete("Short T Name II"))
    }
    it(
      "removes the middle initial if the first and last names  can fit without the middle initial"
    ) {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "TruncateNameForMeF",
          Seq(
            new CompNodeConfigElement(
              "FirstName",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("01234567890123456")
                )
              )
            ),
            new CompNodeConfigElement(
              "MiddleInitial",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("T")
                )
              )
            ),
            new CompNodeConfigElement(
              "LastName",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("0123456789012345")
                )
              )
            ),
            new CompNodeConfigElement(
              "Suffix",
              Seq(
                new CompNodeConfigElement("String")
              )
            )
          )
        )
      )
      assert(
        node.get(0) == Result.Complete("01234567890123456 0123456789012345")
      )
    }
    it(
      "shortens the first name to an initial if the first and last name go over 35 characters"
    ) {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "TruncateNameForMeF",
          Seq(
            new CompNodeConfigElement(
              "FirstName",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("01234567890123456")
                )
              )
            ),
            new CompNodeConfigElement(
              "MiddleInitial",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("T")
                )
              )
            ),
            new CompNodeConfigElement(
              "LastName",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("012345678901234567")
                )
              )
            ),
            new CompNodeConfigElement(
              "Suffix",
              Seq(
                new CompNodeConfigElement("String")
              )
            )
          )
        )
      )
      assert(
        node.get(0) == Result.Complete("0 012345678901234567")
      )
    }
    it(
      "will truncate the last name from the end if the last name and first initial are still over 35 characters"
    ) {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "TruncateNameForMeF",
          Seq(
            new CompNodeConfigElement(
              "FirstName",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("01234567890123456")
                )
              )
            ),
            new CompNodeConfigElement(
              "MiddleInitial",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("T")
                )
              )
            ),
            new CompNodeConfigElement(
              "LastName",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value(
                    "012345678901234567890123456789012345"
                  )
                )
              )
            ),
            new CompNodeConfigElement(
              "Suffix",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("VI")
                )
              )
            )
          )
        )
      )
      assert(
        node.get(0) == Result.Complete("0 01234567890123456789012345678901")
      )
    }
    it(
      "works even if the middle name and suffix are blank"
    ) {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "TruncateNameForMeF",
          Seq(
            new CompNodeConfigElement(
              "FirstName",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("Short")
                )
              )
            ),
            new CompNodeConfigElement(
              "MiddleInitial",
              Seq(
                new CompNodeConfigElement("String")
              )
            ),
            new CompNodeConfigElement(
              "LastName",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("Name")
                )
              )
            ),
            new CompNodeConfigElement(
              "Suffix",
              Seq(
                new CompNodeConfigElement("String")
              )
            )
          )
        )
      )
      assert(node.get(0) == Result.Complete("Short Name"))

    }
  }
