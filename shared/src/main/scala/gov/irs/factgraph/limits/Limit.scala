package gov.irs.factgraph.limits

import gov.irs.factgraph.{FactDictionary, Factual}
import gov.irs.factgraph.compnodes.CompNode.defaultFactories
import gov.irs.factgraph.compnodes.{BooleanNode, CompNode, CompNodeFactory}
import gov.irs.factgraph.definitions.fact.{CompNodeConfigTrait, LimitConfigTrait}

import scala.collection.mutable
import gov.irs.factgraph.monads.Result

trait Limit {
  val limiter: BooleanNode
  val context: LimitContext

  def run()(using Factual): Option[LimitViolation] =
    limiter.get(0) match
      case Result.Complete(response) =>
        if (!response)
          val fact = summon[Factual]
          Some(
            LimitViolation(
              context.limitName,
              fact.path.toString,
              context.limitLevel,
              context.actual.get(0).get.toString,
              context.limit.get(0).get.toString,
            ),
          )
        else None
      case _ => None
}

object Limit {
  private val defaultFactories: Seq[LimitFactory] =
    List(Match, Max, Min, MaxLength, MinLength, MaxCollectionSize)
  private val factories = mutable.Map(defaultFactories.map(_.asTuple)*)
  def register(f: LimitFactory): Unit = factories.addOne(f.asTuple)
  def fromConfig(e: LimitConfigTrait)(using Factual)(using
      FactDictionary,
  ): Limit =
    val factory = factories.getOrElse(
      e.operation,
      throw new UnsupportedOperationException(
        s"${e.operation} is not a registered Limit",
      ),
    )
    factory.fromConfig(e)
}
