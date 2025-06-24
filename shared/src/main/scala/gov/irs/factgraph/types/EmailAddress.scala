package gov.irs.factgraph.types

import scala.beans.BeanProperty
import scala.util.matching.Regex
import scala.scalajs.js.annotation.{JSExportTopLevel, JSExport, JSExportAll}
import upickle.default.{ReadWriter => RW, readwriter}

@JSExportTopLevel("EmailAddress")
// This could be an opaque type = String, but it makes serialization
// surprisingly painful, so a trivial case class
final case class EmailAddress(@BeanProperty email: String) derives RW:
  override def toString(): String = email

object EmailAddress:
  // There are A LOT of ways to validate an email address with a regex
  // However, since we're really relying on our identity provider to do
  // this part, I'm leaving it extremely simple
  val SimpleEmailPattern: Regex = """^(.+@.+)$""".r

  @JSExportTopLevel("EmailAddressFactory")
  def apply(s: String): EmailAddress = s match
    case SimpleEmailPattern(email) => new EmailAddress(email)
    case _ =>
      throw new IllegalArgumentException("Email address must have an @ in it")
