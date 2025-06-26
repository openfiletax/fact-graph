package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.{Explanation, Expression, FactDictionary, Factual}
import gov.irs.factgraph.operators.ReduceOperator
import gov.irs.factgraph.monads.{MaybeVector, Result, Thunk}

object Any extends CompNodeFactory:
  override val Key: String = "Any"

  private val operator = AnyOperator()

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
        "all children of <Any> must be BooleanNodes",
      )

private final class AnyOperator extends ReduceOperator[Boolean]:
  // $COVERAGE-OFF$
  override protected def reduce(x: Boolean, y: Boolean): Boolean = ???
  // $COVERAGE-ON$

  override def apply(
      head: Result[Boolean],
      tail: List[Thunk[Result[Boolean]]],
  ): Result[Boolean] = head match
    case Result.Complete(true) => Result.Complete(true)
    case _                     => accumulator(tail, head)

  @scala.annotation.tailrec
  private def accumulator(
      thunks: List[Thunk[Result[Boolean]]],
      a: Result[Boolean],
  ): Result[Boolean] = thunks match
    case thunk :: thunks =>
      val result = thunk.get

      result match
        case Result.Complete(true) => Result.Complete(true)
        case Result.Placeholder(true) =>
          accumulator(thunks, Result.Placeholder(true))
        case Result.Incomplete =>
          if (a == Result.Placeholder(true)) then
            // any set containing Placeholder(true) returns Placeholder(true),
            // unless it includes a Complete(true) result
            accumulator(thunks, Result.Placeholder(true))
          else
            // otherwise, any set containing Incomplete returns Incomplete
            accumulator(thunks, Result.Incomplete)
        case _ => // Complete(false) or Placeholder(false)
          if (a.complete) then
            // if the accumulated result is Complete(false), we'll use the new
            // result, which is either Complete(false) or Placeholder(false)
            //
            // ACCUMULATED       NEW                  RESULT
            // Complete(false) + Complete(false)    = Complete(false)
            // Complete(false) + Placeholder(false) = Placeholder(false)
            accumulator(thunks, result)
          else
            // otherwise, we'll use the accumulated result, which is either
            // Placeholder(true), Placeholder(false), or Incomplete
            //
            // ACCUMULATED          NEW                  RESULT
            // Placeholder(true)  + Complete(false)    = Placeholder(true)
            // Placeholder(true)  + Placeholder(false) = Placeholder(true)
            // Placeholder(false) + Complete(false)    = Placeholder(false)
            // Placeholder(false) + Placeholder(false) = Placeholder(false)
            // Incomplete         + Complete(false)    = Incomplete
            // Incomplete         + Placeholder(false) = Incomplete
            accumulator(thunks, a)
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
        case Result.Complete(true) =>
          Explanation.opWithInclusiveChildren(xExp)
        case _ =>
          explainRecurse(
            next,
            Explanation.Operation(explanation.children :+ List(xExp)),
          )
    case Nil => explanation
