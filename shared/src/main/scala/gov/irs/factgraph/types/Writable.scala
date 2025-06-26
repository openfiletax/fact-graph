package gov.irs.factgraph.types

// Note: Once you've added a type here, you'll need to add the appropriate wrapper and case
// to the sealed class in shared/src/main/scala/gov/irs/factgraph/persisters/TypeContainer.scala
// so that you can persist and serialize it
// You'll also need to add the Node to WritableNode.scala
type WritableType = Address | BankAccount | Boolean | Byte | Collection | CollectionItem | Day | Dollar | EmailAddress |
  Enum | Ein | E164Number | Int | IpPin | MultiEnum | Pin | Rational | Short | String | Tin
