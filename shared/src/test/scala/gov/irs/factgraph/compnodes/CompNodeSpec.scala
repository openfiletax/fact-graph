package gov.irs.factgraph.compnodes

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.{
  CompNodeConfigElement,
  CompNodeConfigTrait,
  WritableConfigElement
}
import gov.irs.factgraph.monads.Result

class CompNodeSpec extends AnyFunSpec:
  describe("CompNode") {
    describe(".isWritable") {
      describe("when the CompNode is writable") {
        it("returns true") {
          assert(
            IntNode
              .fromWritableConfig(new WritableConfigElement("Int"))
              .isWritable
          )
        }
      }

      describe("when the CompNode is not writable") {
        it("returns false") {
          assert(!IntNode(42).isWritable)
        }
      }
    }

    describe("$register") {
      object DummyFactory extends CompNodeFactory:
        override val Key: String = "Dummy"
        val node: StringNode =
          new StringNode(Expression.Constant(Some("Hello world")))

        override def fromDerivedConfig(e: CompNodeConfigTrait)(using
            Factual
        )(using FactDictionary): CompNode = node

      assertThrows[UnsupportedOperationException] {
        CompNode.fromDerivedConfig(
          new CompNodeConfigElement("Dummy")
        )
      }

      it("learns how to parse new CompNodes from Config") {
        CompNode.register(DummyFactory)

        val node =
          CompNode.fromDerivedConfig(new CompNodeConfigElement("Dummy"))

        assert(node.get(0) == Result.Complete("Hello world"))
      }
    }

    describe("$getConfigChildNode") {
      it("can parse a single, anonymous child") {
        val config = new CompNodeConfigElement(
          "Parent",
          Seq(
            new CompNodeConfigElement("True")
          )
        )
        val node = CompNode.getConfigChildNode(config)

        assert(node.get(0) == Result.Complete(true))
      }

      it("can parse a single, named child") {
        val config = new CompNodeConfigElement(
          "Parent",
          Seq(
            new CompNodeConfigElement(
              "Children",
              Seq(
                new CompNodeConfigElement("True")
              )
            )
          )
        )
        val node = CompNode.getConfigChildNode(config, "Children")

        assert(node.get(0) == Result.Complete(true))
      }

      it("throws an exception unless exactly one node is found") {
        assertThrows[IllegalArgumentException] {
          CompNode.getConfigChildNode(new CompNodeConfigElement("Parent"))
        }

        assertThrows[IllegalArgumentException] {
          val config = new CompNodeConfigElement(
            "Parent",
            Seq(
              new CompNodeConfigElement("True"),
              new CompNodeConfigElement("False")
            )
          )
          CompNode.getConfigChildNode(config)
        }

        assertThrows[IllegalArgumentException] {
          val config = new CompNodeConfigElement(
            "Parent",
            Seq(
              new CompNodeConfigElement("Kinder")
            )
          )
          CompNode.getConfigChildNode(config, "Children")
        }

        assertThrows[IllegalArgumentException] {
          val config = new CompNodeConfigElement(
            "Parent",
            Seq(
              new CompNodeConfigElement("Children")
            )
          )
          CompNode.getConfigChildNode(config, "Children")
        }

        assertThrows[IllegalArgumentException] {
          val config = new CompNodeConfigElement(
            "Parent",
            Seq(
              new CompNodeConfigElement(
                "Children",
                Seq(
                  new CompNodeConfigElement("True"),
                  new CompNodeConfigElement("False")
                )
              )
            )
          )
          CompNode.getConfigChildNode(config, "Children")
        }
      }
    }

    describe("$getConfigChildNodes") {
      it("parses multiple anonymous children") {
        val config = new CompNodeConfigElement(
          "Parent",
          Seq(
            new CompNodeConfigElement("True"),
            new CompNodeConfigElement("False")
          )
        )
        val nodes1 = CompNode.getConfigChildNodes(config)
        assert(nodes1.head.get(0) == Result.Complete(true))
        assert(nodes1(1).get(0) == Result.Complete(false))
      }

      it("parses multiple named children") {
        val config = new CompNodeConfigElement(
          "Parent",
          Seq(
            new CompNodeConfigElement(
              "Children",
              Seq(
                new CompNodeConfigElement("True"),
                new CompNodeConfigElement("False")
              )
            )
          )
        )
        val nodes2 = CompNode.getConfigChildNodes(config, "Children")

        assert(nodes2.head.get(0) == Result.Complete(true))
        assert(nodes2(1).get(0) == Result.Complete(false))
      }

      it("throws an exception unless at least one node is found") {
        assertThrows[IllegalArgumentException] {
          val config = new CompNodeConfigElement("Parent")
          CompNode.getConfigChildNodes(config)
        }

        assertThrows[IllegalArgumentException] {
          val config = new CompNodeConfigElement(
            "Parent",
            Seq(
              new CompNodeConfigElement("Kinder")
            )
          )
          CompNode.getConfigChildNodes(config, "Children")
        }

        assertThrows[IllegalArgumentException] {
          val config = new CompNodeConfigElement(
            "Parent",
            Seq(
              new CompNodeConfigElement("Children")
            )
          )
          CompNode.getConfigChildNodes(config, "Children")
        }
      }
    }
  }
