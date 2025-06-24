package gov.irs.factgraph.types
import upickle.default._
import scala.annotation.targetName
import scala.beans.BeanProperty
import scala.scalajs.js.annotation.{JSExportTopLevel, JSExport, JSExportAll}

implicit val localDateReadWrite: ReadWriter[java.time.LocalDate] =
  readwriter[String]
    .bimap[java.time.LocalDate](_.toString, java.time.LocalDate.parse(_))

@JSExportTopLevel("Day")
final case class Day(@BeanProperty date: java.time.LocalDate) derives ReadWriter:
  import Day.DayIsComparable._

  def <(that: Day): Boolean = lt(this, that)

  def >(that: Day): Boolean = gt(this, that)

  def <=(that: Day): Boolean = lteq(this, that)

  def >=(that: Day): Boolean = gteq(this, that)

  def -(y: Days): Day = this.minusDays(y)

  def minusDays(y: Days): Day = Day(this.date.minusDays(y.longValue))

  @JSExport
  def year: Int = date.getYear()
  @JSExport
  def month: Int = date.getMonth().getValue
  @JSExport
  def day: Int = date.getDayOfMonth()

  override def toString: String = date.toString

object Day:
  def apply(s: String): Day = this(java.time.LocalDate.parse(s))

  def apply(s: Option[String]): Option[Day] =
    s match
      case Some(dateStr) =>
        try Some(apply(dateStr))
        catch case _: java.time.format.DateTimeParseException => None
      case None => None

  implicit object DayIsComparable extends Ordering[Day]:
    def compare(
        x: gov.irs.factgraph.types.Day,
        y: gov.irs.factgraph.types.Day,
    ): Int = x.date.compareTo(y.date)
