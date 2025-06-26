package gov.irs.factgraph

// Only children that are at present determing the result of a node are included
// in the explanation. Children are formatted as lists of lists.
//
// [[A, B]] indicates that both A and B will always be required in order to
// calculate a result, while [[A], [B]] indicates that were the result of A or B
// to independently change, we might no longer require the other one.
//
// Here are some examples to illustrate this concept.
//
// An <Any> node only cares about whether it has a true value; order doesn't
// matter. As a result, a complete, true node will provide only a single child
// explanation, indicating the child that made it true. However, if the node is
// false, or incomplete, it will provide a child explanation for each of its
// children using the [[A], [B]] format, as a change in any of them could affect
// the result, and moot the others.
//
// An <All> node, on the other hand, only cares about whether it has a false
// value. As a result, it works exactly the opposite, providing a single
// explanation if it contains a complete, false node, and explaining everything
// using the [[A], [B]] format if not.
//
// A <Switch> statement must check each case sequentially. Imagine a <Switch>
// with three cases — WhenA, WhenB, and WhenC — each of which has an equivalent
// then statement — ThenA, ThenB, and ThenC, respectively. If WhenA and WhenC
// are false, and only WhenB is true, the node will have the following children:
//
// [[WhenA], [WhenB, ThenB]]
//
// Because a change to the values of ThenA, WhenC, and ThenC would not affect
// the result. On the other hand, if WhenA was false, but WhenB was incomplete,
// the children would be:
//
// [[WhenA], [WhenB]]
//
// This expresses that only a change in WhenA or WhenB could affect the result,
// although a change in either of them could make ThenA, ThenB, WhenC, and/or
// ThenC newly relevant.
//
// A <Placeholder> node only explains its source value; we aren't interested
// in explaining default values.

enum Explanation:
  case Constant
  case Writable(complete: Boolean, path: Path)
  case Operation(childList: List[List[Explanation]])
  case Dependency(
      complete: Boolean,
      source: Path,
      target: Path,
      childList: List[List[Explanation]],
  )
  case NotAttachedToGraph

  def children: List[List[Explanation]] = this match
    case Operation(children)           => children
    case Dependency(_, _, _, children) => children
    case _                             => List()

  def solves: List[List[Path]] = this match
    case Writable(false, path) => List(List(path))
    case _                     =>
      // X = [[A, B], [C, D]]
      // Y = [[E, F]]
      // Z = [[G], [H]]
      //
      // [[X], [Y]] = [[A, B], [C, D], [E, F]]
      // [[X], [Z]] = [[A, B], [C, D], [G], [H]]
      // [[X, Y]] = [[A, B, E, F], [C, D, E, F]]
      // [[X, Z]] = [[A, B, G], [A, B, H], [C, D, G], [C, D, H]]
      // [[X, Y, Z]] = [[A, B, E, F, G], [A, B, E, F, H], [C, D, E, F, G], [C, D, E, F, H]]

      // [[X, Y], [Z]] -> [[[[A, B], [C, D]], [[E, F]]], [[[G], [H]]]]
      val expandedSets = for {
        set <- children
      } yield for {
        explanation <- set
      } yield explanation.solves

      expandedSets.flatMap { (setOfSets) => // [[[A, B], [C, D]], [[E, F]]]
        setOfSets.reduce { (acc, newSets) =>
          // acc: [[A, B], [C, D]]
          // newSets: [[E, F]]
          for {
            set1 <- acc // [A, B]
            set2 <- newSets // [E, F]
          } yield set1 ++ set2
        }
      }

  def incompleteDependencies: List[(Path, Path)] =
    findIncompleteDependencies(List(this), List()).reverse

  @annotation.tailrec
  private def findIncompleteDependencies(
      list: List[Explanation],
      acc: List[(Path, Path)],
  ): List[(Path, Path)] = list match
    case explanation :: next =>
      val children = explanation.children.flatten
      val incompletes = explanation match
        case Dependency(false, source, target, _) => (source, target) +: acc
        case _                                    => acc
      findIncompleteDependencies(children ++ next, incompletes)
    case Nil => acc

object Explanation:
  def opWithInclusiveChildren(children: List[Explanation]): Explanation =
    Explanation.Operation(List(children))

  def opWithInclusiveChildren(children: Explanation*): Explanation =
    opWithInclusiveChildren(children.toList)

  def opWithExclusiveChildren(children: List[Explanation]): Explanation =
    Explanation.Operation(for child <- children yield List(child))

  def opWithExclusiveChildren(children: Explanation*): Explanation =
    opWithExclusiveChildren(children.toList)
