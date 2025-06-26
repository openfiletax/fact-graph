package gov.irs.factgraph.util

object Seq:
  def itemsHaveSameRuntimeClass(seq: Seq[Any]): Boolean = seq match
    case h :: t => t.forall(_.getClass == h.getClass)
    case Nil    => true
