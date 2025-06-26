package gov.irs.factgraph

import gov.irs.factgraph.compnodes.CompNode
import gov.irs.factgraph.monads.*
import gov.irs.factgraph.limits.*

trait Factual:
  def value: CompNode
  def path: Path
  def meta: Factual.Meta
  def size: Factual.Size
  def abstractPath: Path

  def limits: Seq[Limit]
  def get: MaybeVector[Result[?]]
  def getThunk: MaybeVector[Thunk[Result[?]]]
  def explain: MaybeVector[Explanation]

  def isWritable: Boolean = value.isWritable

  def apply(path: Path): MaybeVector[Result[Factual]]
  def apply(key: PathItem): MaybeVector[Result[Factual]]

object Factual:
  enum Size:
    case Single
    case Multiple

  final class Meta(
      val size: Size,
      val abstractPath: Path,
  )
