package gov.irs.factgraph.definitions

import gov.irs.factgraph.definitions.fact.FactConfigTrait
import gov.irs.factgraph.definitions.meta.MetaConfigTrait

case class FactDictionaryConfigElement(
    meta: MetaConfigTrait,
    facts: Seq[FactConfigTrait],
) extends FactDictionaryConfigTrait
