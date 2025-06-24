package gov.irs.factgraph.compnodes

import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.fact.CompNodeConfigElement
import gov.irs.factgraph.monads.Result
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.funsuite.AnyFunSuite

class SwitchSpec extends AnyFunSpec:
  describe("Switch") {
    it("cannot be empty") {
      assertThrows[IllegalArgumentException] {
        CompNode.fromDerivedConfig(new CompNodeConfigElement("Switch"))
      }
    }

    it("must contain a boolean condition") {
      // the behavior present in this test will crash the js application.
      // it is potentially possible to correct this, but the performance hit
      // and file size increase could be massive.
      // For more, see the Undefined Behaviors section
      // https://www.scala-js.org/doc/semantics.html
    }

    it("all results must be of the same type") {
      // the behavior present in this test will crash the js application.
      // it is potentially possible to correct this, but the performance hit
      // and file size increase could be massive.
      // For more, see the Undefined Behaviors section
      // https://www.scala-js.org/doc/semantics.html
    }
  }
