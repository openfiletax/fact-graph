package gov.irs.factgraph.limits

import gov.irs.factgraph.definitions.fact.LimitLevel

case class LimitViolation(
    limitName: String,
    factPath: String,
    LimitLevel: LimitLevel,
    actual: String,
    limit: String,
)
