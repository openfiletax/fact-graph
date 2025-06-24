package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual, Path}
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait, WritableConfigTrait}

final case class MultiEnumNode(
    expr: Expression[gov.irs.factgraph.types.MultiEnum],
    enumOptionsPath: Path,
) extends CompNode:
  type Value = gov.irs.factgraph.types.MultiEnum
  override def ValueClass = classOf[gov.irs.factgraph.types.MultiEnum];

  override private[compnodes] def fromExpression(
      expr: Expression[gov.irs.factgraph.types.MultiEnum],
  ): CompNode =
    MultiEnumNode(expr, enumOptionsPath)

object MultiEnumNode extends CompNodeFactory with WritableNodeFactory:
  override val Key: String = "MultiEnum"

  override def fromWritableConfig(e: WritableConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val enumOptionsPath = e.options.find(x => x.name == "optionsPath")
    if (enumOptionsPath == None) {
      throw new IllegalArgumentException(
        s"MultiEnum must contain ${CommonOptionConfigTraits.ENUM_OPTIONS_PATH}",
      )
    }
    new MultiEnumNode(
      Expression.Writable(classOf[gov.irs.factgraph.types.MultiEnum]),
      Path(enumOptionsPath.get.value),
    )

  def apply(value: gov.irs.factgraph.types.MultiEnum): MultiEnumNode =
    new MultiEnumNode(
      Expression.Constant(Some(value)),
      Path(value.enumOptionsPath),
    )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val dictionary = summon[FactDictionary]
    val enumOptionsPath =
      e.getOptionValue(CommonOptionConfigTraits.ENUM_OPTIONS_PATH)
    val values = e
      .getOptionValue(CommonOptionConfigTraits.VALUE)
      .get
      .split(",")
      .map(_.trim)
      .toSet

    if (enumOptionsPath == None) {
      throw new IllegalArgumentException(
        s"MultiEnum must contain ${CommonOptionConfigTraits.ENUM_OPTIONS_PATH}",
      )
    }

    this(
      gov.irs.factgraph.types.MultiEnum
        .apply(values, enumOptionsPath.get),
    )
