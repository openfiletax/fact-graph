package gov.irs.factgraph.validation

/** The specific reason validation failed
  *
  * This type is intended for internal or programmatic uses
  */
trait ValidationFailureReason:
  /** Represents simplified reason suited for giving to a nontechnical end user
    *
    * Does not provide the actual user-facing message, but is instead used as an i18n key
    */
  type UserFriendlyReason
  def toUserFriendlyReason(): UserFriendlyReason

/** Exception thrown by a type factory when validation is failed
  */
trait ValidationFailure[FailureReason <: ValidationFailureReason] extends IllegalArgumentException:
  val validationMessage: FailureReason
