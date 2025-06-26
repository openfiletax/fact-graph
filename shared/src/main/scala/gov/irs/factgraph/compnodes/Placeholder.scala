package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Explanation, Expression, FactDictionary, Factual}
import gov.irs.factgraph.monads.{MaybeVector, Result, Thunk}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.operators.BinaryOperator

import scala.annotation.unused

object Placeholder extends CompNodeFactory:
  override val Key: String = "Placeholder"

  def apply(source: CompNode, default: CompNode): CompNode =
    if (source.getClass != default.getClass)
      throw new UnsupportedOperationException(
        s"placeholder (${default.getClass.getName}) must be of the same type as the fact for which it is a placeholder (${source.getClass.getName})",
      )

    source.fromExpression(
      Expression.Binary(
        source.expr,
        default.expr,
        summon[PlaceholderOperator[source.Value]]
          .asInstanceOf[BinaryOperator[
            source.Value,
            source.Value,
            default.Value,
          ]],
      ),
    )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val source = CompNode.getConfigChildNode(e, "Source")
    val default = CompNode.getConfigChildNode(e, "Default")

    this(source, default)

  def isWritablePlaceholder[A](expr: Expression[A]): Boolean =
    // A placeholder is writable if and only if the source is writable
    expr match
      // note: the Placeholder is instantiated as a Binary expression using the PlaceholderOperator
      //    No other metadata is available so we have to check the operator to deterine if this is a Placeholder
      case Expression.Binary(lhs, _, op) =>
        op.isInstanceOf[PlaceholderOperator[A]] && lhs.isWritable
      case _ =>
        false

private final class PlaceholderOperator[A] extends BinaryOperator[A, A, A]:
  protected def operation(lhs: A, rhs: A): A = ???

  override def apply(lhs: Result[A], rhs: Thunk[Result[A]]): Result[A] =
    lhs match
      case Result.Incomplete => rhs.get.asPlaceholder
      case _                 => lhs

  override def explain(
      lhs: Expression[_],
      rhs: Expression[_],
  )(using Factual): MaybeVector[Explanation] =
    for {
      explanation <- lhs.explain
    } yield Explanation.opWithInclusiveChildren(explanation)

@unused
private object PlaceholderOperator:
  implicit def operator[A]: PlaceholderOperator[A] = PlaceholderOperator[A]
