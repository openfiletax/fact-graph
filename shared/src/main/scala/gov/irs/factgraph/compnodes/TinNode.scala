package gov.irs.factgraph.compnodes

import gov.irs.factgraph.Expression
import gov.irs.factgraph.Factual
import gov.irs.factgraph.Path
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait, WritableConfigTrait}
import gov.irs.factgraph.types.Tin
import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.PathItem
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.definitions.fact.CommonOptionConfigTraits.allowAllZeros
import scala.scalajs.js.annotation.JSExportAll
import gov.irs.factgraph.Fact
import gov.irs.factgraph.definitions.fact.OptionConfigTrait

@JSExportAll
final case class TinNode(expr: Expression[Tin], allowAllZeros: Boolean) extends CompNode:
  type Value = Tin
  override def ValueClass = classOf[Tin];

  override private[compnodes] def fromExpression(
      expr: Expression[Tin],
  ): CompNode =
    TinNode(expr, allowAllZeros)

  override def extract(key: PathItem): Option[CompNode] = key match
    case PathItem.Child(Symbol("isSSN")) =>
      Some(
        BooleanNode(
          Expression.Extract((x: Result[Tin]) => x.map(_.isSSN)),
        ),
      )
    case PathItem.Child(Symbol("isITIN")) =>
      Some(
        BooleanNode(
          Expression.Extract((x: Result[Tin]) => x.map(_.isITIN)),
        ),
      )
    case PathItem.Child(Symbol("isATIN")) =>
      Some(
        BooleanNode(
          Expression.Extract((x: Result[Tin]) => x.map(_.isATIN)),
        ),
      )
    case _ => None

object TinNode extends CompNodeFactory with WritableNodeFactory:
  override val Key: String = "TIN"

  override def fromWritableConfig(e: WritableConfigTrait)(using
      Factual,
  )(using FactDictionary): CompNode =
    val allowAllZeros = e.options
      .find(x => x.name == CommonOptionConfigTraits.ALLOW_ALL_ZEROS)
      .getOrElse(new OptionConfigTrait:
        override def value: String = "false"
        override def name: String = CommonOptionConfigTraits.ALLOW_ALL_ZEROS,
      )
      .value == "true"

    new TinNode(
      Expression.Writable(classOf[Tin]),
      allowAllZeros,
    )

  def apply(value: Tin): TinNode = new TinNode(
    Expression.Constant(Some(value)),
    value.allowAllZeros,
  )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val allowAllZeros = e
      .getOptionValue(CommonOptionConfigTraits.ALLOW_ALL_ZEROS)
      .getOrElse("false") == "true"
    this(
      Tin(
        e.getOptionValue(CommonOptionConfigTraits.VALUE).get,
        allowAllZeros = allowAllZeros,
      ),
    )
