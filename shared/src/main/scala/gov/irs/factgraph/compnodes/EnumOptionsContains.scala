package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.monads.{MaybeVector, Result, Thunk}
import gov.irs.factgraph.operators.{AggregateOperator, BinaryOperator, UnaryOperator}

object EnumOptionsContains extends CompNodeFactory:
  override val Key: String = "EnumOptionsContains"
  private val enumOperator = EnumContainsOperator()
  private val multiEnumOperator = MultiEnumContainsOperator()
  def apply(node: CompNode, value: EnumNode): BooleanNode =
    node match
      case node: EnumOptionsNode =>
        BooleanNode(
          Expression.Binary(
            node.expr,
            value.expr,
            enumOperator,
          ),
        )
      case node: MultiEnumNode =>
        BooleanNode(
          Expression.Binary(
            node.expr,
            value.expr,
            multiEnumOperator,
          ),
        )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val lhs = CompNode.getConfigChildNode(e, "Options")
    val rhs = CompNode.getConfigChildNode(e, "Value")
    val lhsIsEnumOptions = lhs.isInstanceOf[EnumOptionsNode]
    val lhsIsMultiEnum = lhs.isInstanceOf[MultiEnumNode]
    if ((!lhsIsEnumOptions && !lhsIsMultiEnum) || !rhs.isInstanceOf[EnumNode])
      throw new IllegalArgumentException(
        "Options should be EnumOptions or MultiEnum and Value should be Enum node",
      )
    if (lhsIsEnumOptions)
      this(lhs.asInstanceOf[EnumOptionsNode], rhs.asInstanceOf[EnumNode])
    else this(lhs.asInstanceOf[MultiEnumNode], rhs.asInstanceOf[EnumNode])

private final class EnumContainsOperator() extends BinaryOperator[Boolean, List[String], gov.irs.factgraph.types.Enum]:
  override protected def operation(
      options: List[String],
      enumm: gov.irs.factgraph.types.Enum,
  ): Boolean =
    enumm.value match
      case Some(value) => options.contains(value)
      case None        => true // Not having picked an option is always valid

private final class MultiEnumContainsOperator()
    extends BinaryOperator[
      Boolean,
      gov.irs.factgraph.types.MultiEnum,
      gov.irs.factgraph.types.Enum,
    ]:
  override protected def operation(
      options: gov.irs.factgraph.types.MultiEnum,
      enumm: gov.irs.factgraph.types.Enum,
  ): Boolean =
    enumm.value match
      case Some(value) => options.values.contains(value)
      case None        => true // Not having picked an option is always valid
