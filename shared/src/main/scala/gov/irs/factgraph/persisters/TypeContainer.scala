package gov.irs.factgraph.persisters
import gov.irs.factgraph.types.{
  Address,
  BankAccount,
  Collection,
  CollectionItem,
  Day,
  Dollar,
  Ein,
  Enum,
  EmailAddress,
  E164Number,
  IpPin,
  MultiEnum,
  Pin,
  Rational,
  Tin,
  WritableType,
}

import upickle.default.{write, ReadWriter, readwriter}

sealed abstract class TypeContainer(val item: WritableType) derives ReadWriter

case class AddressWrapper(override val item: Address) extends TypeContainer(item)
case class BankAccountWrapper(override val item: BankAccount) extends TypeContainer(item)
case class CollectionWrapper(override val item: Collection) extends TypeContainer(item)
case class CollectionItemWrapper(override val item: CollectionItem) extends TypeContainer(item)
case class DayWrapper(override val item: Day) extends TypeContainer(item)
case class DollarWrapper(override val item: Dollar) extends TypeContainer(item)
case class EinWrapper(override val item: Ein) extends TypeContainer(item)
case class EmailAddressWrapper(override val item: EmailAddress) extends TypeContainer(item)
case class EnumWrapper(override val item: Enum) extends TypeContainer(item)
case class MultEnumWrapper(override val item: MultiEnum) extends TypeContainer(item)
case class TinWrapper(override val item: Tin) extends TypeContainer(item)
case class E164Wrapper(override val item: E164Number) extends TypeContainer(item)
case class RationalWrapper(override val item: Rational) extends TypeContainer(item)
case class StringWrapper(override val item: String) extends TypeContainer(item)
case class BooleanWrapper(override val item: Boolean) extends TypeContainer(item)
case class IntWrapper(override val item: Int) extends TypeContainer(item)
case class PinWrapper(override val item: Pin) extends TypeContainer(item)
case class IpPinWrapper(override val item: IpPin) extends TypeContainer(item)

object TypeContainer:
  def apply(item: WritableType) =
    item match
      case a: Address          => AddressWrapper(a)
      case b: BankAccount      => BankAccountWrapper(b)
      case c: Collection       => CollectionWrapper(c)
      case ci: CollectionItem  => CollectionItemWrapper(ci)
      case day: Day            => DayWrapper(day)
      case dollar: Dollar      => DollarWrapper(dollar)
      case ein: Ein            => EinWrapper(ein)
      case email: EmailAddress => EmailAddressWrapper(email)
      case tin: Tin            => TinWrapper(tin)
      case e: Enum             => EnumWrapper(e)
      case me: MultiEnum       => MultEnumWrapper(me)
      case p: E164Number       => E164Wrapper(p)
      case r: Rational         => RationalWrapper(r)
      case s: String           => StringWrapper(s)
      case b: Boolean          => BooleanWrapper(b)
      case i: Int              => IntWrapper(i)
      case byte: Byte          => IntWrapper(byte)
      case short: Short        => IntWrapper(short)
      case pin: Pin            => PinWrapper(pin)
      case ippin: IpPin        => IpPinWrapper(ippin)

  def unapply(wrappedItem: TypeContainer): WritableType =
    wrappedItem.item
