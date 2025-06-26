package gov.irs.factgraph.definitions.fact

enum LimitLevel:
  case Warn, Error

trait LimitConfigTrait {
  def operation: String
  def level: LimitLevel
  def node: CompNodeConfigTrait
}
