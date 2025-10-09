package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{ Expression, FactDictionary, Factual }
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.operators.BinaryOperator
import gov.irs.factgraph.types.*

object Modulo extends CompNodeFactory:
  override val Key: String = "Modulo"

  private val intOperator = IntModuloOperator()

  def apply(lhs: CompNode, rhs: CompNode): CompNode =
    (lhs, rhs) match
      case (lhs: IntNode, rhs: IntNode) =>
        IntNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            intOperator,
          ),
        )
      case _ =>
        throw new UnsupportedOperationException(
          s"cannot perform modulo operation on a ${lhs.getClass.getName} and a ${rhs.getClass.getName}",
        )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using
      Factual,
  )(using
      FactDictionary,
  ): CompNode =
    val children = CompNode.getConfigChildNodes(e)
    children match
      case lhs :: rhs :: Nil => this(lhs, rhs)
      case _                 => throw new IllegalArgumentException("Must have two children")

  final private class IntModuloOperator extends BinaryOperator[Int, Int, Int]:
    override protected def operation(lhs: Int, rhs: Int): Int = lhs % rhs
