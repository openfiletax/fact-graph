package gov.irs.factgraph.definitions.fact

import gov.irs.factgraph.exceptions.FactGraphValidationException

class LimitConfig(
    val operation: String,
    val level: LimitLevel,
    val node: CompNodeConfig,
) extends LimitConfigTrait

object LimitConfig {
  def fromXml(node: scala.xml.Node): LimitConfig = {
    val operation = node \@ "type"
    if (operation == "") throw FactGraphValidationException("Limit requires a 'type' attribute")

    val children = (node \ "_").filter(node => !node.isInstanceOf[xml.Comment])
    if (children.length != 1) {
      System.err.println(s"Found children: $children")
      throw FactGraphValidationException("Limit requires exactly 1 child")
    }

    val compNode = CompNodeConfig.fromXml(children.head)

    val level = LimitLevel.Error // Just setting all these to Error for now

    LimitConfig(operation, level, compNode)
  }
}
