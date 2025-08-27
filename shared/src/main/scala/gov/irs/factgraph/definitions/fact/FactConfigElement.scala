package gov.irs.factgraph.definitions.fact

import gov.irs.factgraph.definitions.fact.{ CompNodeConfigTrait, FactConfigTrait, WritableConfigTrait }
import gov.irs.factgraph.exceptions.FactGraphValidationException
import scala.xml.NodeSeq

case class FactConfigElement(
    path: String,
    writable: Option[WritableConfigTrait],
    derived: Option[CompNodeConfigTrait],
    placeholder: Option[CompNodeConfigTrait],
    overrideCondition: Option[CompNodeConfigTrait] = None,
    overrideDefault: Option[CompNodeConfigTrait] = None,
    node: NodeSeq = NodeSeq.Empty,
) extends FactConfigTrait

object FactConfigElement {
  def fromXml(factNode: scala.xml.Node): FactConfigElement = {
    val path = factNode \@ "path"

    // Some facts have <Name>s, but it seems like the Fact Graph doesn't care about that at all, for now
//    print(path)

    // TODO!!
    // Writeable and Derived fact are mutually exclusive - a fact cannot both be writeable and derived
    // This could be modeled in the type system instead of as two "Options"
    // Also, it should probably be two different nodes: WriteableFact and DerivedFact :o
    // Alternatively: <Fact type="writeable"> or <Fact type="derived">
    val writableConfigNodes = factNode \ "Writable"
    val writable = writableConfigNodes.length match {
      case 0 => Option.empty
      case 1 => Option(WritableConfigElement.fromXml(writableConfigNodes.head))
      case _ =>
        throw FactGraphValidationException(
          s"Fact $path has more than one writable node",
        )
    }

    val derivedConfigNodes = factNode \ "Derived"
    val derived = derivedConfigNodes.length match {
      case 0 => Option.empty
      case 1 => Option(readSingleCompNode(derivedConfigNodes.head))
      case _ =>
        throw FactGraphValidationException(
          s"Fact $path has more than one derived node ",
        )
    }

    // Both writeable and derived facts can have placeholders, though
    val placeholderNodes = factNode \ "Placeholder"
    val placeholder = placeholderNodes.length match {
      case 0 => Option.empty
      case 1 => Option(readSingleCompNode(placeholderNodes.head))
      case _ =>
        throw FactGraphValidationException(
          s"Fact $path has more than one placeholder",
        )
    }

    // Both writeable and derived facts can have overrides, though
    val overrideNodes = factNode \ "Override"
    val (overrideCondition, overrideDefault) = overrideNodes.length match {
      case 0 => (Option.empty, Option.empty)
      case 1 =>
        val conditionNode = overrideNodes \ "Condition"
        val condition = Option(readSingleCompNode(conditionNode.head))

        val defaultNode = overrideNodes \ "Default"
        val default = Option(readSingleCompNode(defaultNode.head))
        (condition, default)
      case _ =>
        throw FactGraphValidationException(
          s"Fact $path has more than one override",
        )
    }

    FactConfigElement(
      path,
      writable,
      derived,
      placeholder,
      overrideCondition,
      overrideDefault,
      factNode,
    )
  }
}

// This gets the single child of a <Derived> or <Placeholder>
// TODO <Derived> and <Placeholder> need their own types
def readSingleCompNode(derivedOrPlaceholder: scala.xml.Node) = {
  val children =
    (derivedOrPlaceholder \ "_").filter(node => !node.isInstanceOf[xml.Comment])
  if (children.length != 1) {
    throw FactGraphValidationException(
      s"Derived or Placeholder node has ${children.length} children",
    )
  }
  CompNodeConfig.fromXml(children.head)
}
