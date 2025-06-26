package gov.irs.factgraph.compnodes.transformations

import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.operators.BinaryOperator
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.monads.Thunk
import gov.irs.factgraph.operators.ReduceOperator
import gov.irs.factgraph.operators.Arity4Operator
import gov.irs.factgraph.compnodes.CompNodeFactory
import gov.irs.factgraph.compnodes.StringNode
import gov.irs.factgraph.compnodes.CompNode

object TruncateNameForMeF extends CompNodeFactory:
  override val Key: String = "TruncateNameForMeF"

  private val operator = TruncateNameForMeFOperator()

  def apply(
      firstName: StringNode,
      middleInitial: StringNode,
      lastName: StringNode,
      suffix: StringNode,
  ): StringNode =
    StringNode(
      Expression.Arity4(
        firstName.expr,
        middleInitial.expr,
        lastName.expr,
        suffix.expr,
        operator,
      ),
    )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val firstName = CompNode.getConfigChildNode(e, "FirstName")
    val middleInitial = CompNode.getConfigChildNode(e, "MiddleInitial")
    val lastName = CompNode.getConfigChildNode(e, "LastName")
    val suffix = CompNode.getConfigChildNode(e, "Suffix")
    val nameParts = (firstName :: middleInitial :: lastName :: suffix :: Nil)
    if (nameParts.forall((_.isInstanceOf[StringNode])))
      this(
        firstName.asInstanceOf[StringNode],
        middleInitial.asInstanceOf[StringNode],
        lastName.asInstanceOf[StringNode],
        suffix.asInstanceOf[StringNode],
      )
    else
      throw new UnsupportedOperationException(
        "all children of TruncateNameForMeF must be StringNodes",
      )

private final case class MeFName(
    val firstName: Option[String],
    val middleInitial: Option[String],
    val lastName: Option[String],
    val suffix: Option[String],
):

  def joinName(nameParts: Seq[Option[String | Char]]): String =
    nameParts.flatten.map(_.toString().strip()).filter(_ != "").mkString(" ")
  def fullName = joinName(
    firstName :: middleInitial :: lastName :: suffix :: Nil,
  )
  def nameWithoutMiddleInitial = joinName(
    firstName :: lastName :: suffix :: Nil,
  )
  def nameWithAbbreviatedFirstName = joinName(
    firstInitial :: lastName :: suffix :: Nil,
  )

  def nameLimitedTo35Charcters = nameWithAbbreviatedFirstName.slice(0, 34)

  def firstInitial = firstName.getOrElse("").lift(0)

  def compliantName =
    (fullName :: nameWithoutMiddleInitial :: nameWithAbbreviatedFirstName :: nameLimitedTo35Charcters :: Nil)
      .find(_.length() <= 35)
      .head
      .trim()

private final class TruncateNameForMeFOperator extends Arity4Operator[String, String, String, String, String]:

  // TODO: We should override explain from Arity4Operator to account for allowing
  // args 2 and 4 to be incomplete, but won't at this time due to deadline pressure
  override def operation(
      arg1: String,
      arg2: String,
      arg3: String,
      arg4: String,
  ): String = ???
  override def apply(
      arg1: Result[String],
      arg2: Result[String],
      arg3: Result[String],
      arg4: Result[String],
  ): Result[String] =
    val (completes, values) = (arg1 :: arg2 :: arg3 :: arg4 :: Nil)
      .unzip(r => (r.complete, r.value))
    val List(firstName, middleInitial, lastName, suffix) = values
    val List(
      isFirstNameComplete,
      isMiddleInitialComplete,
      isLastNameComplete,
      isSuffixComplete,
    ) = completes
    val value =
      MeFName(firstName, middleInitial, lastName, suffix).compliantName
    // In this case, completion really just depends on the first and last name
    val complete =
      isFirstNameComplete && isLastNameComplete

    Result(value, complete)
