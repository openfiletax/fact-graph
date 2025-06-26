package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait}
import gov.irs.factgraph.operators.CollectOperator
import gov.irs.factgraph.types.{Collection, CollectionItem}
import gov.irs.factgraph.monads.*
import gov.irs.factgraph.{Expression, FactDictionary, Factual, Path, PathItem}

object Filter extends CompNodeFactory:
  override val Key: String = "Filter"

  private val operator = FilterOperator()

  def apply(path: Path, cnBuilder: Factual ?=> BooleanNode)(using
      fact: Factual,
  ): CompNode =
    fact(path :+ PathItem.Wildcard)(0) match
      case Result.Complete(collectionItem) =>
        CollectionNode(
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

private final class FilterOperator extends CollectOperator[Collection, Boolean]:
  override def apply(
      vect: MaybeVector[(CollectionItem, Thunk[Result[Boolean]])],
  ): Result[Collection] =
    val (items, thunks) = vect.toVector.unzip
    val results = thunks.map(_.get)

    val bools = results.map(_.getOrElse(false))
    val filteredIds = for {
      i <- bools.indices if bools(i)
    } yield items(i).id

    val complete = vect.complete && results.forall(_.complete)

    Result(Collection(filteredIds.toVector), complete)
