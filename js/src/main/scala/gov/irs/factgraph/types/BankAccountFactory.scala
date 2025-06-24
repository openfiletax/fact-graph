package gov.irs.factgraph.types
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.matching.Regex
import gov.irs.factgraph.monads.JSEither
import scala.util.{Try, Success, Failure}

object BankAccountFactory:
  @JSExportTopLevel("BankAccountFactory")
  def apply(
      accountType: String,
      routingNumber: String,
      accountNumber: String,
  ): JSEither[BankAccountValidationFailure, BankAccount] =
    Try(new BankAccount(accountType, routingNumber, accountNumber)) match
      case Success(v)                               => JSEither.Right(v)
      case Failure(e: BankAccountValidationFailure) => JSEither.Left(e)
      case Failure(exception) =>
        JSEither.Left(
          BankAccountValidationFailure(
            "Something unexpected went wrong",
            None.orNull,
            BankAccountFailureReason.InvalidBankAccount,
          ),
        )
