package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait}
import gov.irs.factgraph.operators.CollectOperator
import gov.irs.factgraph.types.CollectionItem
import gov.irs.factgraph.monads.*
import gov.irs.factgraph.{Expression, FactDictionary, Factual, Path, PathItem}

object Find extends CompNodeFactory:
  override val Key: String = "Find"

  private val operator = FindOperator()

  def apply(path: Path, cnBuilder: Factual ?=> BooleanNode)(using
      fact: Factual,
  ): CompNode =
    fact(path :+ PathItem.Wildcard)(0) match
      case Result.Complete(collectionItem) =>
        CollectionItemNode(
          Expression
            .Collect(path, cnBuilder(using collectionItem).expr, operator),
          Some(path),
        )
      case _ =>
        throw new IllegalArgumentException(
          s"cannot find fact at path '$path' from '${fact.path}'",
        )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val cnBuilder: Factual ?=> BooleanNode =
      CompNode.getConfigChildNode(e) match
        case node: BooleanNode => node
        case _ =>
          throw new UnsupportedOperationException(
            s"invalid child type: $e",
          )
    this(Path(e.getOptionValue(CommonOptionConfigTraits.PATH).get), cnBuilder)

private final class FindOperator extends CollectOperator[CollectionItem, Boolean]:
  override def apply(
      vect: MaybeVector[(CollectionItem, Thunk[Result[Boolean]])],
  ): Result[CollectionItem] =
    val item = vect.toVector.collectFirst {
      case (item, thunk) if thunk.get.getOrElse(false) => item
    }

    Result(item)
