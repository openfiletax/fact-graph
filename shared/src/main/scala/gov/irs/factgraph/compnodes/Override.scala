package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{ Explanation, Expression, FactDictionary, Factual }
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.monads.{ MaybeVector, Result, Thunk }
import gov.irs.factgraph.operators.BinaryOperator
import scala.annotation.unused

/** Example usage: <Fact path="/someFact"> <Description>someFact</Description> <Override> <Condition> <Dependency
  * path="/someBooleanFact" /> </Condition> <Default> <True /> </Default> </Override>
  *
  * <Writable> <Boolean /> </Writable> </Fact>
  */
object Override extends CompNodeFactory:
  override val Key: String = "Override"

  def apply(
      source: CompNode,
      condition: BooleanNode,
      default: CompNode,
  ): CompNode =
    val expression = Expression.Switch(
      List(
        (
          condition.expr,
          default.expr.asInstanceOf[Expression[source.Value]],
        ),
        (Expression.Constant(Some(true)), source.expr),
      ),
    )
    source.fromExpression(
      Expression.Binary(
        source.expr,
        expression,
        OverrideOperator(),
      ),
    )

  def isWritableOverride[A](expr: Expression[A]): Boolean =
    expr match
      // note: the Override is instantiated as a Unary expression using the OverrideOperator
      // No other metadata is available so we have to check the operator
      case Expression.Binary(lhs, _, op) =>
        op.isInstanceOf[OverrideOperator[A]] && lhs.isWritable
      case _ =>
        false

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using
      Factual,
  )(using
      FactDictionary,
  ): CompNode =
    throw new UnsupportedOperationException(
      "Override should be used directly as a child of a fact",
    )

final private class OverrideOperator[A] extends BinaryOperator[A, A, A]:
  override protected def operation(lhs: A, rhs: A): A = rhs
  override def apply(lhs: Result[A], rhs: Thunk[Result[A]]): Result[A] = rhs.get
