package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.operators.{BinaryOperator}

import scala.util.matching.Regex
import scala.quoted.runtime.Patterns.patternType

object StripChars extends CompNodeFactory:
  override val Key: String = "StripChars"
  private val replaceOperator = ReplaceOperator()

  def strip(
      input: String,
      allow: String,
  ): String =
    // Strip all but allowed characters
    val disallowedChars = "[^" + allow + "]"
    disallowedChars.r.replaceAllIn(input, "")

  def apply(input: StringNode, allow: StringNode): StringNode =
    StringNode(
      Expression.Binary(
        input.expr,
        allow.expr,
        replaceOperator,
      ),
    )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val lhs = CompNode.getConfigChildNode(e, "Input")
    val rhs = CompNode.getConfigChildNode(e, "Allow")
    if (!lhs.isInstanceOf[StringNode] || !rhs.isInstanceOf[StringNode])
      throw new IllegalArgumentException(
        "Input and Allow should be string nodes",
      )
    this(lhs.asInstanceOf[StringNode], rhs.asInstanceOf[StringNode])

private final class ReplaceOperator() extends BinaryOperator[String, String, String]:
  override protected def operation(input: String, allow: String): String =
    StripChars.strip(input, allow)
