package gov.irs.factgraph.compnodes

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.{
  WritableConfigElement,
  WritableConfigTrait
}
import gov.irs.factgraph.monads.Result

class WritableNodeSpec extends AnyFunSpec:
  describe("WritableNode") {
    describe("$register") {
      object DummyNode extends WritableNodeFactory:
        override val Key: String = "Dummy"
        override def fromWritableConfig(e: WritableConfigTrait)(using
            Factual
        )(using FactDictionary): CompNode =
          new IntNode(Expression.Writable(classOf[Int]))

      assertThrows[UnsupportedOperationException] {
        WritableNode.fromConfig(new WritableConfigElement("Dummy"))
      }

      it("learns how to parse new WritableNodes from config") {
        WritableNode.register(DummyNode)

        val node = WritableNode.fromConfig(new WritableConfigElement("Dummy"))
        assert(node.isWritable)
      }
    }
  }
