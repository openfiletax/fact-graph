package gov.irs.factgraph

import gov.irs.factgraph.*
import gov.irs.factgraph.definitions.fact.*

package object compnodes:

  given FactDictionary = FactDictionary()
  given Factual = FactDefinition.fromConfig(
    FactConfigElement(
      "/test",
      None,
      Some(
        new CompNodeConfigElement(
          "Int",
          Seq.empty,
          CommonOptionConfigTraits.value("42")
        )
      ),
      None
    )
  )
