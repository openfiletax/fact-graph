package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.operators.UnaryOperator
import gov.irs.factgraph.types.{Dollar, Enum, EmailAddress, Ein, Tin}

object AsString extends CompNodeFactory:
  override val Key: String = "AsString"

  private val enumOperator = EnumAsStringOperator()
  private val emailAddressOperator = EmailAsStringOperator()
  private val dollarOperator = DollarAsStringOperator()
  private val einOperator = EinAsStringOperator()
  private val tinOperator = TinAsStringOperator()

  def apply(node: CompNode): StringNode =
    node match
      case node: EnumNode =>
        StringNode(
          Expression.Unary(
            node.expr,
            enumOperator,
          ),
        )
      case node: EmailAddressNode =>
        StringNode(
          Expression.Unary(
            node.expr,
            emailAddressOperator,
          ),
        )
      case node: DollarNode =>
        StringNode(
          Expression.Unary(
            node.expr,
            dollarOperator,
          ),
        )
      case node: EinNode =>
        StringNode(
          Expression.Unary(
            node.expr,
            einOperator,
          ),
        )
      case node: TinNode =>
        StringNode(
          Expression.Unary(
            node.expr,
            tinOperator,
          ),
        )
      case _ =>
        throw new UnsupportedOperationException(
          s"cannot execute AsString on a ${node.getClass.getName}",
        )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    CompNode.getConfigChildNode(e) match
      case x: EnumNode         => this(x)
      case x: EmailAddressNode => this(x)
      case x: DollarNode       => this(x)
      case x: EinNode          => this(x)
      case x: TinNode          => this(x)
      case _ =>
        throw new UnsupportedOperationException(
          s"invalid child type: ${e.typeName}",
        )

private final class EnumAsStringOperator extends UnaryOperator[String, Enum]:
  override protected def operation(x: Enum): String = x.getValue()

private final class EmailAsStringOperator extends UnaryOperator[String, EmailAddress]:
  override protected def operation(x: EmailAddress): String = x.toString()

private final class DollarAsStringOperator extends UnaryOperator[String, Dollar]:
  override protected def operation(x: Dollar): String = x.toString()

private final class EinAsStringOperator extends UnaryOperator[String, Ein]:
  override protected def operation(x: Ein): String = x.toString()

private final class TinAsStringOperator extends UnaryOperator[String, Tin]:
  override protected def operation(x: Tin): String = x.toString()
