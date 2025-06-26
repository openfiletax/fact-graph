package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{FactDefinition, FactDictionary, Graph}
import gov.irs.factgraph.definitions.fact.{
  CommonOptionConfigTraits,
  CompNodeConfigElement,
  FactConfigElement,
  WritableConfigElement
}
import gov.irs.factgraph.monads.Result
import org.scalatest.funspec.AnyFunSpec

class StripCharsSpec extends AnyFunSpec:
  describe("StripChars") {
    it("allowed chars in simple list") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "StripChars",
          Seq(
            new CompNodeConfigElement(
              "Input",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("abcdef")
                )
              )
            ),
            new CompNodeConfigElement(
              "Allow",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("cea")
                )
              )
            )
          )
        )
      )

      assert(node.get(0) == Result.Complete(String("ace")))
    }

    it("allowed chars in regex groups") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "StripChars",
          Seq(
            new CompNodeConfigElement(
              "Input",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("s&a(p%e'r#a$v@i)")
                )
              )
            ),
            new CompNodeConfigElement(
              "Allow",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("a-z")
                )
              )
            )
          )
        )
      )

      assert(node.get(0) == Result.Complete(String("saperavi")))
    }

    it("allowed chars with escaped pattern") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "StripChars",
          Seq(
            new CompNodeConfigElement(
              "Input",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value(" back\\slash ")
                )
              )
            ),
            new CompNodeConfigElement(
              "Allow",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("a-z\\\\")
                )
              )
            )
          )
        )
      )

      assert(node.get(0) == Result.Complete(String("back\\slash")))
    }

    it("allowed chars for TextType") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "StripChars",
          Seq(
            new CompNodeConfigElement(
              "Input",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value(
                    " ![El niño Ángel tiene £50]*ç "
                  )
                )
              )
            ),
            new CompNodeConfigElement(
              "Allow",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("!-~£§ÁÉÍÑÓ×ÚÜáéíñóúü")
                )
              )
            )
          )
        )
      )

      assert(node.get(0) == Result.Complete(String("![ElniñoÁngeltiene£50]*")))
    }
    it("allowed chars for BusinessNameLine1Type") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "StripChars",
          Seq(
            new CompNodeConfigElement(
              "Input",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value(
                    "#1 Biz & sons - well 2/4 or 50% of them(.*~;\")"
                  )
                )
              )
            ),
            new CompNodeConfigElement(
              "Allow",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("A-Za-z0-9#/%\\-\\(\\)'& ")
                )
              )
            )
          )
        )
      )

      assert(
        node.get(0) == Result.Complete(
          String("#1 Biz & sons - well 2/4 or 50% of them()")
        )
      )
    }

    it("can handle associated dependencies rather than direct regex") {
      val dictionary = FactDictionary();
      FactDefinition.fromConfig(
        FactConfigElement(
          "/numberString",
          Some(new WritableConfigElement("String")),
          None,
          None
        )
      )(using dictionary)
      FactDefinition.fromConfig(
        FactConfigElement(
          "/regex",
          None,
          Some(
            new CompNodeConfigElement(
              "String",
              Seq.empty,
              CommonOptionConfigTraits.value("[0-9]")
            )
          ),
          None
        )
      )(using dictionary)
      FactDefinition.fromConfig(
        FactConfigElement(
          "/applyRegex",
          None,
          Some(
            new CompNodeConfigElement(
              "StripChars",
              Seq(
                new CompNodeConfigElement(
                  "Input",
                  Seq(
                    new CompNodeConfigElement(
                      "Dependency",
                      Seq.empty,
                      CommonOptionConfigTraits.path("/numberString")
                    )
                  )
                ),
                new CompNodeConfigElement(
                  "Allow",
                  Seq(
                    new CompNodeConfigElement("Dependency", Seq.empty, "/regex")
                  )
                )
              )
            )
          ),
          None
        )
      )(using dictionary)

      val graph = Graph(dictionary)
      graph.set("/numberString", "123-456-7890")
      graph.save();
      var ret = graph.get("/applyRegex");
      assert(ret.complete == true)
      assert(ret.value.get == "1234567890")
    }
  }
