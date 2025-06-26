package gov.irs.factgraph.types

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("Days")
opaque type Days = Int

extension (x: Days) def longValue: Long = x.toLong

object Days:
  def apply(d: Int): Days = d
  def apply(s: String): Days = s.toInt
