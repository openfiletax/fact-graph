package gov.irs.factgraph.compnodes

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.*
import gov.irs.factgraph.definitions.fact.{
  CommonOptionConfigTraits,
  CompNodeConfigElement,
  FactConfigElement,
  WritableConfigElement
}
import gov.irs.factgraph.monads.Result

class EnumOptionsNodeSpec extends AnyFunSpec:
  describe("EnumOptionsNodeSpec") {
    describe("$apply") {
      it("creates nodes from unqualified values") {
        val optionsNode = EnumOptionsNode(List("A", "B"))

        val ret = optionsNode.get(0)

        assert(ret == Result.Complete(List("A", "B")));
      }

      it("creates nodes from qualified values") {
        val trueNode = BooleanNode(true)
        val falseNode = BooleanNode(false)
        val enumValA = StringNode("A")
        val enumValB = StringNode("B")
        val enumValC = StringNode("C")
        val enumConditions =
          List(
            (trueNode, enumValA),
            (falseNode, enumValB),
            (trueNode, enumValC)
          )
        val optionsNode = EnumOptionsNode(enumConditions)

        val ret = optionsNode.get(0)

        assert(ret == Result.Complete(List("A", "C")));
      }
    }

    describe("$fromDerivedConfig") {
      it("builds from unqualified config") {
        val dictionary = FactDictionary.apply()

        val config = FactConfigElement(
          "/options",
          None,
          Some(
            new CompNodeConfigElement(
              "EnumOptions",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("A")
                ),
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("B")
                ),
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("C")
                )
              ),
              Seq.empty
            )
          ),
          None
        )

        FactDefinition.fromConfig(config)(using dictionary)
        val graph = Graph(dictionary)
        val options = graph(Path("/options"))(0).get
        assert(options.value.isInstanceOf[EnumOptionsNode])

        assert(options.value.get(0) == Result.Complete(List("A", "B", "C")))
      }

      it("builds from qualified config") {
        val dictionary = FactDictionary.apply()

        val config = FactConfigElement(
          "/options",
          None,
          Some(
            new CompNodeConfigElement(
              "EnumOptions",
              Seq(
                new CompNodeConfigElement(
                  "EnumOption",
                  Seq(
                    CompNodeConfigElement(
                      "Condition",
                      Seq(
                        new CompNodeConfigElement("True")
                      ),
                      Seq.empty
                    ),
                    CompNodeConfigElement(
                      "Value",
                      Seq(
                        CompNodeConfigElement(
                          "String",
                          Seq.empty,
                          CommonOptionConfigTraits.value("A")
                        )
                      ),
                      Seq.empty
                    )
                  )
                ),
                new CompNodeConfigElement(
                  "EnumOption",
                  Seq(
                    CompNodeConfigElement(
                      "Condition",
                      Seq(
                        new CompNodeConfigElement("True")
                      ),
                      Seq.empty
                    ),
                    CompNodeConfigElement(
                      "Value",
                      Seq(
                        CompNodeConfigElement(
                          "String",
                          Seq.empty,
                          CommonOptionConfigTraits.value("B")
                        )
                      ),
                      Seq.empty
                    )
                  )
                ),
                new CompNodeConfigElement(
                  "EnumOption",
                  Seq(
                    CompNodeConfigElement(
                      "Condition",
                      Seq(
                        new CompNodeConfigElement("False")
                      ),
                      Seq.empty
                    ),
                    CompNodeConfigElement(
                      "Value",
                      Seq(
                        CompNodeConfigElement(
                          "String",
                          Seq.empty,
                          CommonOptionConfigTraits.value("C")
                        )
                      ),
                      Seq.empty
                    )
                  )
                )
              ),
              Seq.empty
            )
          ),
          None
        )

        FactDefinition.fromConfig(config)(using dictionary)
        val graph = Graph(dictionary)
        val options = graph(Path("/options"))(0).get
        assert(options.value.isInstanceOf[EnumOptionsNode])

        assert(options.value.get(0) == Result.Complete(List("A", "B")))
      }

      it("builds from a mix of qualified and unqualified config") {
        val dictionary = FactDictionary.apply()

        val config = FactConfigElement(
          "/options",
          None,
          Some(
            new CompNodeConfigElement(
              "EnumOptions",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("As qualified as Alex")
                ),
                new CompNodeConfigElement(
                  "EnumOption",
                  Seq(
                    CompNodeConfigElement(
                      "Condition",
                      Seq(
                        new CompNodeConfigElement("True")
                      ),
                      Seq.empty
                    ),
                    CompNodeConfigElement(
                      "Value",
                      Seq(
                        CompNodeConfigElement(
                          "String",
                          Seq.empty,
                          CommonOptionConfigTraits.value("A")
                        )
                      ),
                      Seq.empty
                    )
                  )
                ),
                new CompNodeConfigElement(
                  "EnumOption",
                  Seq(
                    CompNodeConfigElement(
                      "Condition",
                      Seq(
                        new CompNodeConfigElement("True")
                      ),
                      Seq.empty
                    ),
                    CompNodeConfigElement(
                      "Value",
                      Seq(
                        CompNodeConfigElement(
                          "String",
                          Seq.empty,
                          CommonOptionConfigTraits.value("B")
                        )
                      ),
                      Seq.empty
                    )
                  )
                ),
                new CompNodeConfigElement(
                  "EnumOption",
                  Seq(
                    CompNodeConfigElement(
                      "Condition",
                      Seq(
                        new CompNodeConfigElement("False")
                      ),
                      Seq.empty
                    ),
                    CompNodeConfigElement(
                      "Value",
                      Seq(
                        CompNodeConfigElement(
                          "String",
                          Seq.empty,
                          CommonOptionConfigTraits.value("C")
                        )
                      ),
                      Seq.empty
                    )
                  )
                )
              ),
              Seq.empty
            )
          ),
          None
        )

        FactDefinition.fromConfig(config)(using dictionary)
        val graph = Graph(dictionary)
        val options = graph(Path("/options"))(0).get
        assert(options.value.isInstanceOf[EnumOptionsNode])

        assert(
          options.value.get(0) == Result.Complete(
            List("As qualified as Alex", "A", "B")
          )
        )
      }
    }

    describe("$qualifications") {
      it("can qualify based on a dependent variable") {
        val dictionary = FactDictionary.apply()
        val predicateConfig = FactConfigElement(
          "/predicate",
          None,
          Some(new CompNodeConfigElement("True")),
          None
        )
        FactDefinition.fromConfig(predicateConfig)(using dictionary)

        val config = FactConfigElement(
          "/options",
          None,
          Some(
            new CompNodeConfigElement(
              "EnumOptions",
              Seq(
                new CompNodeConfigElement(
                  "EnumOption",
                  Seq(
                    CompNodeConfigElement(
                      "Condition",
                      Seq(
                        new CompNodeConfigElement(
                          "Dependency",
                          Seq.empty,
                          CommonOptionConfigTraits.path("/predicate")
                        )
                      ),
                      Seq.empty
                    ),
                    CompNodeConfigElement(
                      "Value",
                      Seq(
                        CompNodeConfigElement(
                          "String",
                          Seq.empty,
                          CommonOptionConfigTraits.value("A")
                        )
                      ),
                      Seq.empty
                    )
                  )
                ),
                new CompNodeConfigElement(
                  "EnumOption",
                  Seq(
                    CompNodeConfigElement(
                      "Condition",
                      Seq(
                        new CompNodeConfigElement("True")
                      ),
                      Seq.empty
                    ),
                    CompNodeConfigElement(
                      "Value",
                      Seq(
                        CompNodeConfigElement(
                          "String",
                          Seq.empty,
                          CommonOptionConfigTraits.value("B")
                        )
                      ),
                      Seq.empty
                    )
                  )
                ),
                new CompNodeConfigElement(
                  "EnumOption",
                  Seq(
                    CompNodeConfigElement(
                      "Condition",
                      Seq(
                        new CompNodeConfigElement("False")
                      ),
                      Seq.empty
                    ),
                    CompNodeConfigElement(
                      "Value",
                      Seq(
                        CompNodeConfigElement(
                          "String",
                          Seq.empty,
                          CommonOptionConfigTraits.value("C")
                        )
                      ),
                      Seq.empty
                    )
                  )
                )
              ),
              Seq.empty
            )
          ),
          None
        )

        FactDefinition.fromConfig(config)(using dictionary)
        val graph = Graph(dictionary)
        val options = graph.get("/options")
        assert(options.value.get == List("A", "B"))
      }
    }

    it("returns incomplete if a predicate is incomplete") {
      val dictionary = FactDictionary.apply()
      FactDefinition.fromConfig(
        FactConfigElement(
          "/predicate",
          Some(
            new WritableConfigElement("Int")
          ),
          None,
          None
        )
      )(using dictionary)
      val config = FactConfigElement(
        "/options",
        None,
        Some(
          new CompNodeConfigElement(
            "EnumOptions",
            Seq(
              new CompNodeConfigElement(
                "EnumOption",
                Seq(
                  CompNodeConfigElement(
                    "Condition",
                    Seq(
                      new CompNodeConfigElement(
                        "Equal",
                        Seq(
                          new CompNodeConfigElement(
                            "Left",
                            Seq(
                              new CompNodeConfigElement(
                                "Int",
                                Seq.empty,
                                CommonOptionConfigTraits.value("1")
                              )
                            )
                          ),
                          new CompNodeConfigElement(
                            "Right",
                            Seq(
                              new CompNodeConfigElement(
                                "Dependency",
                                Seq.empty,
                                CommonOptionConfigTraits.path("/predicate")
                              )
                            )
                          )
                        )
                      )
                    ),
                    Seq.empty
                  ),
                  CompNodeConfigElement(
                    "Value",
                    Seq(
                      CompNodeConfigElement(
                        "String",
                        Seq.empty,
                        CommonOptionConfigTraits.value("A")
                      )
                    ),
                    Seq.empty
                  )
                )
              ),
              new CompNodeConfigElement(
                "EnumOption",
                Seq(
                  CompNodeConfigElement(
                    "Condition",
                    Seq(
                      new CompNodeConfigElement("False")
                    ),
                    Seq.empty
                  ),
                  CompNodeConfigElement(
                    "Value",
                    Seq(
                      CompNodeConfigElement(
                        "String",
                        Seq.empty,
                        CommonOptionConfigTraits.value("C")
                      )
                    ),
                    Seq.empty
                  )
                )
              )
            ),
            Seq.empty
          )
        ),
        None
      )

      FactDefinition.fromConfig(config)(using dictionary)
      val graph = Graph(dictionary)
      val options = graph.get("/options")
      assert(options == Result.Incomplete)
    }
  }
