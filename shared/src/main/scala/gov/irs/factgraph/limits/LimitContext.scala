package gov.irs.factgraph.limits

import gov.irs.factgraph.compnodes.CompNode
import gov.irs.factgraph.definitions.fact.LimitLevel

case class LimitContext(
    limitName: String,
    limitLevel: LimitLevel,
    actual: CompNode,
    limit: CompNode,
)
