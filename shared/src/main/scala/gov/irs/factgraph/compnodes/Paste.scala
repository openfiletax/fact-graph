package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait}
import gov.irs.factgraph.operators.ReduceOperator

object Paste extends CompNodeFactory:
  override val Key: String = "Paste"

  private val defaultSeparator = " "
  private val defaultOperator = PasteOperator(defaultSeparator)

  def apply(nodes: Seq[StringNode], sep: String): StringNode =
    val operator =
      if (sep == defaultSeparator) defaultOperator
      else PasteOperator(sep)

    StringNode(Expression.Reduce(nodes.map(_.expr).toList, operator))

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val conditions = CompNode.getConfigChildNodes(e)

    if (conditions.forall(_.isInstanceOf[StringNode]))
      this(
        conditions.asInstanceOf[Seq[StringNode]],
        e.getOptionValue(CommonOptionConfigTraits.SEPARATOR)
          .getOrElse(defaultSeparator),
      )
    else
      throw new UnsupportedOperationException(
        "all children of Paste must be StringNodes",
      )

private final class PasteOperator(val sep: String) extends ReduceOperator[String]:
  override protected def reduce(x: String, y: String): String =
    if x.isEmpty then y
    else if y.isEmpty then x
    else s"$x$sep$y"
