package gov.irs.factgraph.definitions.meta

trait EnumDeclarationTrait {
  def id: String
  def options: Iterable[EnumDeclarationOptionsTrait]
}
