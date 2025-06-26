package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual, Path}
import gov.irs.factgraph.definitions.fact.WritableConfigTrait
import gov.irs.factgraph.types.CollectionItem
import scala.scalajs.js.annotation.JSExport

final case class CollectionItemNode(
    expr: Expression[CollectionItem],
    alias: Option[Path],
) extends CompNode:
  type Value = CollectionItem
  override def ValueClass = classOf[CollectionItem]

  @JSExport
  def getAlias() = alias

  override private[compnodes] def switch(
      cases: List[(BooleanNode, CompNode)],
  ): CompNode =
    val aliasesMatch = cases.forall(
      _._2.asInstanceOf[CollectionItemNode].alias == alias,
    )

    if (!aliasesMatch)
      throw new UnsupportedOperationException(
        "collection items in a <Switch> must reference the same collection",
      )

    CollectionItemNode(
      Expression.Switch(
        cases
          .asInstanceOf[List[(BooleanNode, CollectionItemNode)]]
          .map((b, a) => (b.expr, a.expr)),
      ),
      alias,
    )

  override private[compnodes] def dependency(path: Path): CompNode =
    val newAlias = alias match
      case Some(_) => alias
      case None    => Some(path)

    CollectionItemNode(Expression.Dependency(path), newAlias)

  override private[factgraph] def fromExpression(
      expr: Expression[CollectionItem],
  ): CompNode =
    CollectionItemNode(expr, alias)

object CollectionItemNode extends WritableNodeFactory:
  override val Key: String = "CollectionItem"

  override def fromWritableConfig(
      e: WritableConfigTrait,
  )(using Factual)(using FactDictionary): CompNode =
    new CollectionItemNode(
      Expression.Writable(classOf[CollectionItem]),
      Some(Path(e.collectionItemAlias.get)),
    )
