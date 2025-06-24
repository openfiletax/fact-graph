package gov.irs.factgraph.compnodes
import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.{Expression, FactDictionary, Factual, Path}
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait, WritableConfigTrait}
import gov.irs.factgraph.operators.AggregateOperator
import gov.irs.factgraph.monads.MaybeVector
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.monads.Thunk
final case class EnumOptionsNode(expr: Expression[List[String]]) extends CompNode:
  type Value = List[String]
  // below is equivalent to `Nil.getClass().asInstanceOf[Class[List[String]]]` due to scala List implementation
  override def ValueClass =
    List[String]().getClass().asInstanceOf[Class[List[String]]]

  override private[compnodes] def fromExpression(
      expr: Expression[List[String]],
  ): CompNode =
    EnumOptionsNode(expr)

object EnumOptionsNode extends CompNodeFactory:
  override val Key: String = "EnumOptions"
  def apply(
      options: List[(BooleanNode, StringNode)],
  )(using Factual): CompNode =
    val allowedOpts = Expression.ConditionalList(
      options.map(o => (o._1.expr, o._2.expr)),
    )
    EnumOptionsNode(allowedOpts)

  def apply(options: List[String]): CompNode =
    EnumOptionsNode(Expression.Constant(Some(options)))

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    try {
      val conditions = for {
        c <- e.children.filter(x => (x.typeName == "EnumOption" || x.typeName == "String"))
      } yield (
        c.typeName match
          case "String" =>
            (
              BooleanNode(true),
              StringNode(
                Expression.Constant(
                  c.getOptionValue(CommonOptionConfigTraits.VALUE),
                ),
              ),
            )
          case "EnumOption" =>
            (
              CompNode
                .getConfigChildNode(c, "Condition")
                .asInstanceOf[BooleanNode],
              CompNode.getConfigChildNode(c, "Value").asInstanceOf[StringNode],
            )
      )

      if (conditions.isEmpty) {
        throw new IllegalArgumentException(
          s"EnumOptions must have at least one child node: $e",
        )
      }
      this(conditions.toList)
    } catch {
      case e: ClassCastException =>
        throw new UnsupportedOperationException(
          s"Condition must be boolean: $e",
        )
    }
