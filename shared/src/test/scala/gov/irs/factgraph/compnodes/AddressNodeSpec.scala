package gov.irs.factgraph.compnodes

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.*
import gov.irs.factgraph.definitions.fact.*
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.Address

import gov.irs.factgraph.compnodes.AddressNode
import gov.irs.factgraph.types.AddressValidationFailure
class AddressNodeSpec extends AnyFunSpec:
  describe("AddressNode") {
    describe("$apply") {
      it("creates nodes from values") {
        val address =
          new Address(
            "736 Jackson Place NW",
            "Washington",
            "20503",
            "DC"
          )
        val node = AddressNode(
          address
        )
        assert(
          node.get(0) == Result.Complete(
            address
          )
        )
      }
    }

    describe("$fromDerivedConfig") {
      it("parses config") {
        val config = new CompNodeConfigElement(
          "Address",
          Seq.empty,
          CommonOptionConfigTraits.value(
            "736 Jackson Place NW\nWashington, DC 20503"
          )
        )
        val node = CompNode.fromDerivedConfig(config).asInstanceOf[AddressNode]
        assert(
          node.get(0) == Result.Complete(
            new Address("736 Jackson Place NW", "Washington", "20503", "DC")
          )
        )
      }
    }

    describe(".switch") {
      it("can be used inside a switch statement") {
        val config = new CompNodeConfigElement(
          "Switch",
          Seq(
            new CompNodeConfigElement(
              "Case",
              Seq(
                new CompNodeConfigElement(
                  "When",
                  Seq(new CompNodeConfigElement("False"))
                ),
                new CompNodeConfigElement(
                  "Then",
                  Seq(
                    new CompNodeConfigElement(
                      "Address",
                      Seq.empty,
                      CommonOptionConfigTraits.value(
                        "736 Jackson Place NW\nWashington, DC 20503"
                      )
                    )
                  )
                )
              )
            ),
            new CompNodeConfigElement(
              "Case",
              Seq(
                new CompNodeConfigElement(
                  "When",
                  Seq(new CompNodeConfigElement("True"))
                ),
                new CompNodeConfigElement(
                  "Then",
                  Seq(
                    new CompNodeConfigElement(
                      "Address",
                      Seq.empty,
                      CommonOptionConfigTraits.value(
                        "718 Jackson Place NW\nWashington, DC 20503"
                      )
                    )
                  )
                )
              )
            )
          )
        )
        val node = CompNode.fromDerivedConfig(config)

        assert(
          node.get(0) == Result.Complete(
            Address(
              "718 Jackson Place NW\nWashington, DC 20503"
            )
          )
        )
      }
    }

    describe(".dependency") {
      val dictionary = FactDictionary()

      FactDefinition.fromConfig(
        FactConfigElement(
          "/value",
          None,
          Some(
            new CompNodeConfigElement(
              "Address",
              Seq.empty,
              CommonOptionConfigTraits.value(
                "736 Jackson Place NW\nWashington, DC 20503"
              )
            )
          ),
          None
        )
      )(using dictionary)

      FactDefinition.fromConfig(
        FactConfigElement(
          "/dependent",
          None,
          Some(
            new CompNodeConfigElement(
              "Dependency",
              Seq.empty,
              CommonOptionConfigTraits.path("../value")
            )
          ),
          None
        )
      )(using dictionary)

      val graph = Graph(dictionary)
      val dependent = graph(Path("/dependent"))(0).get

      it("can be depended on by another fact") {
        assert(dependent.value.isInstanceOf[AddressNode])
        assert(
          dependent.get(0) == Result.Complete(
            new Address(
              "736 Jackson Place NW",
              "Washington",
              "20503",
              "DC"
            )
          )
        )
      }
    }

    describe(".fromExpression") {
      it("can create a new node with an expression") {
        val node = AddressNode(Expression.Constant(None))
        val newNode = node.fromExpression(
          Expression.Constant(
            Some(
              new Address("736 Jackson Place NW", "Washington", "20503", "DC")
            )
          )
        )

        assert(
          newNode.get(0) == Result.Complete(
            new Address(
              "736 Jackson Place NW",
              "Washington",
              "20503",
              "DC"
            )
          )
        )
      }
    }

    describe("$writablefromDerivedConfig") {
      it("can read and write a value") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Address")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Incomplete)

        fact.set(
          new Address("736 Jackson Place NW", "Washington", "20503", "DC")
        )
        graph.save()

        assert(
          fact.get(0) == Result.Complete(
            new Address(
              "736 Jackson Place NW",
              "Washington",
              "20503",
              "DC"
            )
          )
        )
      }
    }

    describe("can use sub paths to work with data and") {
      it("can gather the street address") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Address")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        var incomplete = graph(Path("/test/streetAddress"))(0).get
        assert(incomplete.get(0) == Result.Incomplete)
        val fact = graph(Path("/test"))(0).get
        fact.set(
          new Address("736 Jackson Place NW", "Washington", "20503", "DC")
        )
        graph.save()
        var address = graph.get("/test/streetAddress")
        assert(address.get == "736 Jackson Place NW")
      }

      it("can gather the city") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Address")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        var incomplete = graph(Path("/test/city"))(0).get
        assert(incomplete.get(0) == Result.Incomplete)
        val fact = graph(Path("/test"))(0).get
        fact.set(
          new Address("736 Jackson Place NW", "Washington City", "20503", "DC")
        )
        graph.save()
        var address = graph.get("/test/city")
        assert(address.get == "Washington City")
      }

      it("can gather the state") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Address")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        var incomplete = graph(Path("/test/stateOrProvence"))(0).get
        assert(incomplete.get(0) == Result.Incomplete)
        val fact = graph(Path("/test"))(0).get
        fact.set(
          new Address("736 Jackson Place NW", "Washington", "20503", "DC")
        )
        graph.save()
        var address = graph.get("/test/stateOrProvence")
        assert(address.get == "DC")
      }

      it("can gather the postal code") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Address")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        var incomplete = graph(Path("/test/postalCode"))(0).get
        assert(incomplete.get(0) == Result.Incomplete)
        val fact = graph(Path("/test"))(0).get
        fact.set(
          new Address("736 Jackson Place NW", "Washington", "20503", "DC")
        )
        graph.save()
        var address = graph.get("/test/postalCode")
        assert(address.get == "20503")
      }

      it("can gather the country") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Address")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        var incomplete = graph(Path("/test/country"))(0).get
        assert(incomplete.get(0) == Result.Incomplete)
        val fact = graph(Path("/test"))(0).get
        fact.set(
          new Address(
            "736 Jackson Place NW",
            "Washington",
            "20503",
            "DC",
            "",
            "USA"
          )
        )
        graph.save()
        var address = graph.get("/test/country")
        assert(address.get == "USA")
      }

      it("can gather the unit number") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Address")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        var incomplete = graph(Path("/test/streetAddressLine2"))(0).get
        assert(incomplete.get(0) == Result.Incomplete)
        val fact = graph(Path("/test"))(0).get
        fact.set(
          new Address("736 Jackson Place NW", "Washington", "20503", "DC", "2")
        )
        graph.save()
        var address = graph.get("/test/streetAddressLine2")
        assert(address.get == "2")
      }

      it("can determine whether it is a foreign address") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Address")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        var incomplete = graph(Path("/test/foreignAddress"))(0).get
        assert(incomplete.get(0) == Result.Incomplete)
        val fact = graph(Path("/test"))(0).get
        fact.set(
          new Address(
            "736 Jackson Place NW",
            "Washington",
            "20503",
            "DC",
            "",
            "United States of America"
          )
        )
        graph.save()
        var address = graph.get("/test/foreignAddress")
        assert(address.get == false)
      }
    }
  }
