package gov.irs.factgraph.definitions.fact

import gov.irs.factgraph.definitions.fact.{CompNodeConfigTrait, FactConfigTrait, WritableConfigTrait}

case class FactConfigElement(
    path: String,
    writable: Option[WritableConfigTrait],
    derived: Option[CompNodeConfigTrait],
    placeholder: Option[CompNodeConfigTrait],
) extends FactConfigTrait
