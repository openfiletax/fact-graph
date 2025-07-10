package gov.irs.factgraph.definitions.fact

import gov.irs.factgraph.exceptions.FactGraphValidationException
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("WritableConfigElement")
case class WritableConfigElement(
    typeName: String,
    options: Iterable[OptionConfigTrait],
    limits: Iterable[LimitConfigTrait],
    collectionItemAlias: Option[String],
) extends WritableConfigTrait {
  def this(typeName: String) = this(typeName, Seq.empty, Seq.empty, None)
  def this(typeName: String, collectionItemAlias: String) =
    this(typeName, Seq.empty, Seq.empty, Some(collectionItemAlias))
  def this(typeName: String, options: Iterable[OptionConfigTrait]) =
    this(typeName, options, Seq.empty, None)
  def this(
      typeName: String,
      options: Iterable[OptionConfigTrait],
      limits: Iterable[LimitConfigTrait],
  ) = this(typeName, options, limits, None)
}

object WritableConfigElement {
  def fromXml(node: scala.xml.Node): WritableConfigElement = {
    val limits = (node \ "Limit").map(LimitConfig.fromXml)

    val nonLimitNodes = (node \ "_")
      .filter(node => node.label != "Limit")
      .filter(node => !node.isInstanceOf[xml.Comment])

    val writableNode = nonLimitNodes.length match {
      case 0 => throw FactGraphValidationException("Writable node is missing a non-Limit child")
      case 1 => nonLimitNodes.head
      case _ => throw FactGraphValidationException("Writable node has more than 1 non-Limit child")
    }

    val typeName = writableNode.label

    // The Java XML processor has some logic here for extracting text from the children
    // of the writable node. As best I can tell, writable nodes don't have any children
    // find . -name '*.xml' | xargs -n 1 xpath -e '//Writable/*[not(self::Limit)]' 2> /dev/null
    val options = OptionConfigTrait.fromXml(writableNode)

    var collectionItemAlias: Option[String] = Option.empty
    if (typeName == "CollectionItem") {
      val collection = writableNode \@ "collection"
      if (collection != "") {
        collectionItemAlias = Option(collection)
      }
    }

    WritableConfigElement(typeName, options, limits, collectionItemAlias)
  }
}
