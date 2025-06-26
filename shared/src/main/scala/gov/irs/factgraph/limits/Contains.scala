package gov.irs.factgraph.limits

import gov.irs.factgraph.compnodes.{BooleanNode}

case class ContainsLimit(limiter: BooleanNode, context: LimitContext) extends Limit

object ContainsLimit {
  val LimitName = "ContainsLimit";
}
