package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual, Path}
import gov.irs.factgraph.limits.Limit
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait, WritableConfigTrait}
import gov.irs.factgraph.limits.ContainsLimit
import gov.irs.factgraph.definitions.fact.LimitLevel
import gov.irs.factgraph.definitions.fact.CompNodeConfigElement
import gov.irs.factgraph.limits.LimitContext

final case class EnumNode(
    expr: Expression[gov.irs.factgraph.types.Enum],
    enumOptionsPath: String,
) extends CompNode:
  type Value = gov.irs.factgraph.types.Enum
  override def ValueClass = classOf[gov.irs.factgraph.types.Enum]

  override private[compnodes] def fromExpression(
      expr: Expression[gov.irs.factgraph.types.Enum],
  ): CompNode =
    EnumNode(expr, enumOptionsPath)

object EnumNode extends CompNodeFactory with WritableNodeFactory:
  override val Key: String = "Enum"

  override def fromWritableConfig(e: WritableConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val dictionary = summon[FactDictionary]
    val enumOptionsPath = e.options.find(x => x.name == "optionsPath")
    if (enumOptionsPath == None) {
      throw new IllegalArgumentException(
        s"Enum must contain ${CommonOptionConfigTraits.ENUM_OPTIONS_PATH}",
      )
    }
    new EnumNode(
      Expression.Writable(classOf[gov.irs.factgraph.types.Enum]),
      enumOptionsPath.get.value,
    )

  def apply(value: gov.irs.factgraph.types.Enum): EnumNode = new EnumNode(
    Expression.Constant(Some(value)),
    value.enumOptionsPath,
  )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val dictionary = summon[FactDictionary]
    val enumOptionsPath =
      e.getOptionValue(CommonOptionConfigTraits.ENUM_OPTIONS_PATH)
    val value = e.getOptionValue(CommonOptionConfigTraits.VALUE)

    if (value == None) {
      throw new IllegalArgumentException(
        s"Enum must contain ${CommonOptionConfigTraits.VALUE}",
      )
    }

    if (enumOptionsPath == None) {
      throw new IllegalArgumentException(
        s"Enum must contain ${CommonOptionConfigTraits.ENUM_OPTIONS_PATH}",
      )
    }

    this(
      gov.irs.factgraph.types.Enum
        .apply(value.get, enumOptionsPath.get),
    )
