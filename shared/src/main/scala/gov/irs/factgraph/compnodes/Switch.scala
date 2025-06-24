package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait

object Switch extends CompNodeFactory:
  override val Key: String = "Switch"

  def apply(cases: Seq[(BooleanNode, CompNode)]): CompNode =
    val (_, thens) = cases.unzip

    try {
      thens.head.switch(cases.toList)
    } catch {
      case e: ClassCastException =>
        throw new UnsupportedOperationException(
          s"cannot switch between nodes of different types",
        )
    }

  override def fromDerivedConfig(
      e: CompNodeConfigTrait,
  )(using Factual)(using FactDictionary): CompNode =
    try {
      val cases = for {
        c <- e.children.filter(x => x.typeName == "Case")
      } yield (
        CompNode.getConfigChildNode(c, "When").asInstanceOf[BooleanNode],
        CompNode.getConfigChildNode(c, "Then"),
      )

      if (cases.isEmpty)
        throw new IllegalArgumentException(
          s"Switch must have at least one child node: $e",
        )

      this(cases.toSeq)
    } catch {
      case e: ClassCastException =>
        throw new UnsupportedOperationException(
          s"When must be boolean: $e",
        )
    }
