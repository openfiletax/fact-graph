package gov.irs.factgraph.utils

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("unwrapScalaOptional")
def unwrapScalaOptional[T](opt: Option[T]) =
  opt match
    case Some(value) => value
    case _           => null

@JSExportTopLevel("scalaListToJsArray")
def scalaSeqToJsArray[T](seq: List[T]): js.Array[T] =
  import js.JSConverters._
  seq.toJSArray

@JSExportTopLevel("scalaMapToJsMap")
def scalaMapToJsMap[K, V](map: collection.Map[K, V]): js.Map[K, V] =
  import js.JSConverters._
  map.toJSMap

@JSExportTopLevel("jsArrayToScalaList")
def jsArrayToScalaList[T](seq: js.Array[T]): List[T] =
  import js.JSConverters._
  seq.toList

  import js.JSConverters._
  seq.toList

@JSExportTopLevel("jsSetToScalaSet")
def jsSetToScalaSet[T](seq: js.Set[T]): Set[T] =
  import js.JSConverters._
  seq.toSet

@JSExportTopLevel("scalaSetToJsSet")
def scalaSetToJsSet[T](seq: Set[T]): js.Set[T] =
  import js.JSConverters._
  seq.toJSSet
