package gov.irs.factgraph.util

object Math:
  @annotation.tailrec
  def gcd(a: Int, b: Int): Int = a match
    case 0 => b.abs
    case n => gcd(b % a, a)
