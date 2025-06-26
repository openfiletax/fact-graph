package gov.irs.factgraph.definitions.fact

import gov.irs.factgraph.exceptions.FactGraphValidationException
import org.w3c.dom.Node


case class CompNodeConfig(
    typeName: String,
    children: Iterable[CompNodeConfigTrait],
    options: Iterable[OptionConfigTrait],
) extends CompNodeConfigTrait

object CompNodeConfig {
  def fromXml(node: scala.xml.Node): CompNodeConfig = {
    val typeName = node.label
    val children = (node \ "_").map(CompNodeConfig.fromXml)
    
    // We could do this validation if we had more granular comp node types, like Writeable Node Child
    // if (node.length != 1) throw FactGraphValidationException("Writable's comp node should have exactly 1 child")
    
    val options = OptionConfigTrait.fromXml(node)
    CompNodeConfig(typeName, children, options)
  }
} 
