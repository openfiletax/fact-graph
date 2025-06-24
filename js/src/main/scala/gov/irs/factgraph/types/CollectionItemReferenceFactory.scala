package gov.irs.factgraph.types

import scala.scalajs.js.annotation.{JSExport, JSExportAll, JSExportTopLevel}
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.monads.JSEither
import gov.irs.factgraph.validation.{ValidationFailure, ValidationFailureReason}
import gov.irs.factgraph.Graph

@JSExportAll
enum CollectionItemReferenceFailureReason extends ValidationFailureReason:
  type UserFriendlyReason = CollectionItemReferenceFailureReason
  case InvalidItem
  case EmptyCollection
  case InvalidCollection
  case NotACollection

  def toUserFriendlyReason() =
    this

@JSExportAll
final case class CollectionItemReferenceFailure(
    private val message: String = "",
    private val cause: Throwable = None.orNull,
    val validationMessage: CollectionItemReferenceFailureReason,
) extends IllegalArgumentException(message, cause),
      ValidationFailure[CollectionItemReferenceFailureReason];

object CollectionItemReferenceFactory:
  @JSExportTopLevel("CollectionItemReferenceFactory")
  def apply(
      value: String,
      collectionPath: String,
      factGraph: Graph,
  ): JSEither[CollectionItemReferenceFailure, CollectionItem] =
    if (value.length > 0) {
      factGraph.get(collectionPath) match
        case Result.Incomplete =>
          JSEither.Left(
            CollectionItemReferenceFailure(
              "Attempt to reference an empty collection",
              None.orNull,
              CollectionItemReferenceFailureReason.EmptyCollection,
            ),
          )
        case Result.Complete(col: Collection) =>
          col.items.map(_.toString()).contains(value) match
            case true => JSEither.Right(CollectionItemFactory.apply(value))
            case false =>
              JSEither.Left(
                CollectionItemReferenceFailure(
                  "Attempt to reference item not in collection",
                  None.orNull,
                  CollectionItemReferenceFailureReason.InvalidItem,
                ),
              )
        case Result.Placeholder(col: Collection) =>
          col.items.map(_.toString()).contains(value) match
            case true => JSEither.Right(CollectionItemFactory.apply(value))
            case false =>
              JSEither.Left(
                CollectionItemReferenceFailure(
                  "Attempt to reference item not in collection",
                  None.orNull,
                  CollectionItemReferenceFailureReason.InvalidItem,
                ),
              )
        case _ =>
          JSEither.Left(
            CollectionItemReferenceFailure(
              "Attempt to reference a path that isn't a collection",
              None.orNull,
              CollectionItemReferenceFailureReason.NotACollection,
            ),
          )
    } else {
      JSEither.Left(
        CollectionItemReferenceFailure(
          "Blank Collection Item ID",
          None.orNull,
          CollectionItemReferenceFailureReason.InvalidItem,
        ),
      )
    }
