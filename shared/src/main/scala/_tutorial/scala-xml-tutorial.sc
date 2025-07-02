// Scala contains first-class support for XML.
// https://javadoc.io/static/org.scala-lang.modules/scala-xml_2.13/2.4.0/scala/xml/Atom.html
// (search for "def \" on that above page)

// Let's say you had the following XML:
val fact = {
  <Fact path="/cannotDeductStudentLoanInterestBecauseIncomeOrFilingStatus">
    <Name>Cannot deduct student loan interest because of income or filing status</Name>
    <Description>The TP is ineligible to deduct student loan interest because of income or filing status</Description>

    <Derived>
      <Any>
        <Dependency module="filingStatus" path="/isFilingStatusMFS"/>
        <Dependency path="/incomeTooHighForStudentLoanInterestDeductionNotMFJ"/>
        <Dependency path="/incomeTooHighForStudentLoanInterestDeductionMFJ"/>
      </Any>
    </Derived>
  </Fact>
}

// The \\ operator matches all descendents, regardless of depth
fact \\ "Any"
fact \\ "Dependency"

// The \ operator matches all direct children
fact \ "Name"
fact \ "Dependency"
fact \\ "Any" \ "Dependency"

// The \@ will get the attribute of a Node
fact \@ "path"
// This is just syntactic sugar for \ "@path"
// So you can also use \\ to get all descendant attributes
fact \\ "@path"
// This works on Node sequences as well, as long as they have length 1
fact \\ "Fact" \@ "path"
// This will not work, because it's a sequence of length > 1 (yes, that's a weird limitation)
fact \\ "Dependency" \@ "path"


// Note also that document order is preserved
fact \\ "Dependency"

// And keep in mind that these always return sequences
fact \\ "Any" \ "Dependency"


// That's about it
