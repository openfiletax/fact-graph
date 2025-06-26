package gov.irs.factgraph.compnodes

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.*
import gov.irs.factgraph.definitions.fact.*
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.BankAccount

import gov.irs.factgraph.compnodes.BankAccountNode
import gov.irs.factgraph.types.BankAccountValidationFailure
class BankAccountNodeSpec extends AnyFunSpec:
  def buildBankAccount(
      accountType: String = "Checking",
      routingNumber: String = "323075880", // DC Credit Union
      accountNumber: String = "12345"
  ) =
    new BankAccount(accountType, routingNumber, accountNumber)

  def buildNode(): BankAccountNode = BankAccountNode(buildBankAccount())

  describe("BankAccountNode") {
    describe("$apply") {
      it("creates nodes from values") {
        val node = buildNode()

        assert(
          node.get(0) == Result.Complete(buildBankAccount())
        )
      }
    }

    describe(".fromExpression") {
      it("can create a new node with an expression") {
        val node = BankAccountNode(Expression.Constant(None))
        val newNode = node.fromExpression(
          Expression.Constant(Some(buildBankAccount()))
        )

        assert(
          newNode.get(0) == Result.Complete(
            buildBankAccount()
          )
        )
      }
    }

    describe("can use sub paths to work with data and") {
      def buildGraph() =
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("BankAccount")
            ),
            None,
            None
          )
        )(using dictionary)

        Graph(dictionary)

      it("can gather the accountType") {
        val expectedFactValue = buildBankAccount()
        val graph = buildGraph()

        var incomplete = graph(Path("/test/accountType"))(0).get
        assert(incomplete.get(0) == Result.Incomplete)

        val fact = graph(Path("/test"))(0).get
        fact.set(expectedFactValue)
        graph.save()

        var accountType = graph.get("/test/accountType")
        assert(accountType.get == expectedFactValue.accountType)
      }

      it("can gather the routingNumber") {
        val expectedFactValue = buildBankAccount()
        val graph = buildGraph()

        var incomplete = graph(Path("/test/routingNumber"))(0).get
        assert(incomplete.get(0) == Result.Incomplete)

        val fact = graph(Path("/test"))(0).get
        fact.set(expectedFactValue)
        graph.save()

        var routingNumber = graph.get("/test/routingNumber")
        assert(routingNumber.get == expectedFactValue.routingNumber)
      }

      it("can gather the accountNumber") {
        val expectedFactValue = buildBankAccount()
        val graph = buildGraph()

        var incomplete = graph(Path("/test/accountNumber"))(0).get
        assert(incomplete.get(0) == Result.Incomplete)

        val fact = graph(Path("/test"))(0).get
        fact.set(expectedFactValue)
        graph.save()

        var accountNumber = graph.get("/test/accountNumber")
        assert(accountNumber.get == expectedFactValue.accountNumber)
      }
    }
  }
