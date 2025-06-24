package gov.irs.factgraph.definitions.fact

import gov.irs.factgraph.definitions.fact.WritableConfigElement

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.scalajs.js
import gov.irs.factgraph.definitions.fact.{LimitConfigTrait, LimitLevel}

class LimitConfig(
    val operation: String,
    val level: LimitLevel,
    val node: CompNodeConfig,
) extends LimitConfigTrait

class LimitConfigDigestWrapper(
    val operation: String,
    val level: String,
    val node: CompNodeConfigDigestWrapper,
) extends js.Object

class WritableConfigElementDigestWrapper(
    val typeName: String,
    val options: js.Dictionary[String],
    val collectionItemAlias: String | Null,
    val limits: js.Array[LimitConfigDigestWrapper],
) extends js.Object

object WritableConfigElementDigestWrapper:
  def makeNativeLimit(limitConfig: LimitConfigDigestWrapper): LimitConfigTrait =
    val level = LimitLevel.valueOf(limitConfig.level)
    new LimitConfig(
      limitConfig.operation,
      level,
      CompNodeDigestWrapper.toNative(limitConfig.node),
    )

  def toNative(
      wrapper: WritableConfigElementDigestWrapper,
  ): WritableConfigElement =
    val collectionItemAlias = wrapper.collectionItemAlias match
      case null => None
      case _    => Some(wrapper.collectionItemAlias)
    WritableConfigElement(
      wrapper.typeName,
      wrapper.options.map((key, value) => OptionConfig.create(key, value)),
      wrapper.limits.map((limitWrapper) => makeNativeLimit(limitWrapper)),
      collectionItemAlias,
    )
