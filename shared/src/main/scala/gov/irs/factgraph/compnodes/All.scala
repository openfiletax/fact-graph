package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.{Explanation, Expression, FactDictionary, Factual}
import gov.irs.factgraph.operators.ReduceOperator
import gov.irs.factgraph.monads.{MaybeVector, Result, Thunk}

object All extends CompNodeFactory:
  override val Key: String = "All"

  private val operator = AllOperator()

  def apply(nodes: Seq[BooleanNode]): BooleanNode =
    BooleanNode(Expression.Reduce(nodes.map(_.expr).toList, operator))

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val conditions = CompNode.getConfigChildNodes(e)

    if (conditions.forall(_.isInstanceOf[BooleanNode]))
      this(conditions.asInstanceOf[Seq[BooleanNode]])
    else
      throw new UnsupportedOperationException(
        "all children of <All> must be BooleanNodes",
      )

private final class AllOperator extends ReduceOperator[Boolean]:
  // $COVERAGE-OFF$
  override protected def reduce(x: Boolean, y: Boolean): Boolean = ???
  // $COVERAGE-ON$

  override def apply(
      head: Result[Boolean],
      tail: List[Thunk[Result[Boolean]]],
  ): Result[Boolean] = head match
    case Result.Complete(false) => Result.Complete(false)
    case _                      => accumulator(tail, head)

  @scala.annotation.tailrec
  private def accumulator(
      thunks: List[Thunk[Result[Boolean]]],
      a: Result[Boolean],
  ): Result[Boolean] = thunks match
    case thunk :: thunks =>
      thunk.get match
        case Result.Complete(false) => Result.Complete(false)
        case Result(value, complete) =>
          accumulator(
            thunks,
            a.flatMap(aValue => Result(value && aValue, complete)),
          )
        case _ => accumulator(thunks, Result.Incomplete)
    case Nil => a

  override def explain(
      xs: List[Expression[_]],
  )(using Factual): MaybeVector[Explanation] =
    val caseVectors = xs.map(x => (x.getThunk, x.explain))

    MaybeVector.vectorizeListTuple2(
      cases => explainRecurse(cases, Explanation.Operation(List())),
      caseVectors,
    )

  @annotation.tailrec
  private def explainRecurse(
      cases: List[(Thunk[Result[_]], Explanation)],
      explanation: Explanation,
  ): Explanation = cases match
    case (x, xExp) :: next =>
      x.get match
        case Result.Complete(false) =>
          Explanation.opWithInclusiveChildren(xExp)
        case _ =>
          explainRecurse(
            next,
            Explanation.Operation(explanation.children :+ List(xExp)),
          )
    case Nil => explanation
