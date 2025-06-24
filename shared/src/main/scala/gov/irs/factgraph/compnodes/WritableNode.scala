package gov.irs.factgraph.compnodes

import scala.collection.mutable
import gov.irs.factgraph.{FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.WritableConfigTrait

object WritableNode:
  // Note: When adding your type here, also remember to add its
  // underlying type (if you added a new one) to the type union
  // in shared/src/main/scala/gov/irs/factgraph/types/Writable.scala
  private val defaultNodes: Seq[WritableNodeFactory] = List(
    AddressNode,
    BankAccountNode,
    BooleanNode,
    CollectionNode,
    CollectionItemNode,
    DayNode,
    DollarNode,
    EinNode,
    EmailAddressNode,
    EnumNode,
    MultiEnumNode,
    IntNode,
    IpPinNode,
    PhoneNumberNode,
    PinNode,
    RationalNode,
    StringNode,
    TinNode,
  )

  private val nodes = mutable.Map(defaultNodes.map(n => (n.Key, n))*)

  def register(n: WritableNodeFactory): Unit = nodes(n.Key) = n

  def fromConfig(e: WritableConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val factory = nodes.getOrElse(
      e.typeName,
      throw new UnsupportedOperationException(
        s"${e.typeName} is not a registered WritableNode",
      ),
    )
    factory.fromWritableConfig(e)
