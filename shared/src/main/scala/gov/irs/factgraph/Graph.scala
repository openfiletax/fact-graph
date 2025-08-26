package gov.irs.factgraph

import Console.{GREEN, RED, RESET, YELLOW}
import fs2.{Fallible, Stream}
import fs2.data.xml.*
import fs2.data.xml.dom.*
import fs2.data.xml.scalaXml.*
import gov.irs.factgraph.limits.LimitViolation
import gov.irs.factgraph.monads.*
import gov.irs.factgraph.persisters.*
import gov.irs.factgraph.types.{Collection, CollectionItem, WritableType, Enum}
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.collection.mutable
import scala.scalajs.js.annotation.JSExportAll
import java.util.UUID
import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.XML
import scala.xml.PrettyPrinter
import scala.xml.Text
import scala.xml.Elem

class Graph(val dictionary: FactDictionary, val persister: Persister):
  val root: Fact = Fact(this)

  private[factgraph] val factCache = mutable.HashMap[Path, Option[Fact]]()
  private[factgraph] val resultCache =
    mutable.HashMap[Path, MaybeVector[Result[Any]]]()
  private val overriddenFacts: mutable.Map[Path, WritableType] = mutable.Map()

  export root.apply

  def getOverridenFacts(): mutable.Map[Path, WritableType] = overriddenFacts

  @JSExport("get")
  def get(path: String): Result[Any] = get(Path(path))

  @JSExport("getWithPath")
  def get(path: Path): Result[Any] = getVect(path) match
    case MaybeVector.Single(result) => result
    case MaybeVector.Multiple(_, _) =>
      throw new UnsupportedOperationException(
        s"must use getVect to access '$path'",
      )

  @JSExport("getVect")
  def getVect(path: String): MaybeVector[Result[Any]] = getVect(Path(path))
  @JSExport("getVectWithPath")
  def getVect(path: Path): MaybeVector[Result[Any]] =
    for {
      fact <- this(path)
      values <- fact match
        case Result(fact, complete) =>
          if (complete) fact.get
          else fact.get.map(_.asPlaceholder)
        case _ =>
          throw new UnsupportedOperationException(
            s"path '$path' was not found",
          )
    } yield values

  @JSExport("explain")
  def explain(path: String): Explanation = explain(Path(path))
  @JSExport("explainWithPath")
  def explain(path: Path): Explanation =
    val explanations = for {
      fact <- this(path)
      explanation <- fact match
        // Note that we are discarding the completeness of the path's
        // resolution; we are providing an explanation of a *fact's* result,
        // not why a particular *path* returns a potentially incomplete result.
        case Result(fact, _) =>
          fact.explain
        case _ =>
          throw new UnsupportedOperationException(
            s"path '$path' was not found",
          )
    } yield explanation

    explanations match
      case MaybeVector.Single(explanation) => explanation
      case MaybeVector.Multiple(_, _) =>
        throw new UnsupportedOperationException(
          s"path '$path' resolves to a vector",
        )

  @JSExport
  def set(path: String, value: WritableType): (Boolean, Seq[LimitViolation]) = {
    set(Path(path), value)
  }

  def set(path: Path, value: WritableType): (Boolean, Seq[LimitViolation]) = {
    for {
      result <- this(path)
      fact <- result
    } fact.set(value)
    this.save()
  }

  @JSExport
  def addToCollection(path: String, collectionId: String): Unit = {
    val uuid = UUID.fromString(collectionId)

    val collection = this.get(path) match
      case Result.Complete(v) => v.asInstanceOf[Collection]
      case Result.Placeholder(_) => throw new UnsupportedOperationException(s"path $path is a collection with a placeholder")
      case Result.Incomplete => Collection(Vector.empty[UUID])

    val newItems = collection.items :+ uuid
    this.set(path, Collection(newItems))
  }

  @JSExport
  def removeFromCollection(path: String, collectionId: String): Unit = {
    val uuid = UUID.fromString(collectionId)

    val collection = this.get(path) match
      case Result.Complete(v) => v.asInstanceOf[Collection]
      case Result.Placeholder(_) => throw new UnsupportedOperationException(s"path $path is a collection with a placeholder")
      case Result.Incomplete => Collection(Vector.empty[UUID])

    val newItems = collection.items.filter(item => item != uuid)
    this.set(path, Collection(newItems))
  }

  @JSExport
  def delete(path: String): (Boolean, Seq[LimitViolation]) = {
    delete(Path(path))
  }

  @JSExport("deleteWithPath")
  def delete(path: Path): (Boolean, Seq[LimitViolation]) =
    for {
      result <- this(path)
      fact <- result
    } fact.delete()
    this.save()

  def checkPersister(): Seq[PersisterSyncIssue] =
    persister.syncWithDictionary(this)

  def save(): (Boolean, Seq[LimitViolation]) =
    factCache.clear()
    resultCache.clear()

    val out = persister.save()

    // Don't cache invalid results
    if !out._1 then resultCache.clear()

    out

  @JSExport("getDictionary")
  def getDictionary() = this.dictionary

  def getCollectionPaths(collectionPath: String): Seq[String] =
    val paths = for
      pathPerhapsWithWildcards <- Path(collectionPath).populateWildcards(this)
      pathWithoutWildcards <- pathPerhapsWithWildcards.populateWildcards(this)
    yield pathWithoutWildcards.toString

    paths.toSeq

  private def transformFactPath(currentPath: String, originalPath: String): String =
    var pathPrefix = Path(originalPath).asAbstract.toString.split("\\*")(0)
    val collectionId = Path(originalPath).getMemberId match
      case Some(value) => "#" + value
      case None        => null

    var pathString = currentPath
    if (collectionId != null) {
      if (pathString.startsWith("..")) {
        val pathEnding = pathString.replace("..", "")
        pathString = pathPrefix + collectionId + pathEnding
      } else {
        pathString = pathString.replace("*", collectionId)
      }
    }
    pathString

  private def getFactValueFromNode(node: NodeSeq, originalPath: String): String =
    val path = node \ "@path"
    var pathString = path.toString
    pathString = transformFactPath(pathString, originalPath)
    val currentFact = this.get(Path(pathString))
    val currentFactValue = if (currentFact.hasValue && currentFact.complete) {
      s"${RESET}${GREEN}${currentFact.get.toString}${RESET}"
    } else if (currentFact.hasValue && !currentFact.complete) {
      s"${YELLOW}${currentFact.get.toString}"
    } else {
      s"${RED}Incomplete"
    }
    currentFactValue

  private def prettifyFactWithValue(node: Node, originalPath: String, input: String): String =
    val currentFactValue = getFactValueFromNode(node, originalPath)
    val stringifiedNode = node.toString
    val factStartingNode = stringifiedNode.split(">", 2)(0) + ">"
    val output = input.replace(factStartingNode, s"$factStartingNode ⮕ $currentFactValue")
    output

  private def prettifyDependencyWithValue(node: Node, originalPath: String, input: String): String =
    if (Path((node \ "@path").toString).isWildcard) return input

    val currentFactValue = getFactValueFromNode(node, originalPath)
    val stringifiedNode = node.toString
    val output = input.replace(stringifiedNode, s"$stringifiedNode ⮕ $currentFactValue")
    output

  @JSExport
  def debugFact(originalPath: String): Unit =
    dictionary.getDefinitionsAsNodes().get(Path(originalPath).asAbstract) match
      case Some(value) => {
        var debugString = value.toString
        debugString = prettifyFactWithValue(value.head, originalPath, debugString)
        val dependencyNodes = value \\ "Dependency"
        val uniqueDeps = dependencyNodes.distinct
        uniqueDeps.foreach { node =>
          debugString = prettifyDependencyWithValue(node, originalPath, debugString)
        }
        println(debugString)
      }
      case None => throw new Exception(s"Invalid path: $originalPath")

  @JSExport
  def debugFactRecurse(originalPath: String): Unit =
    def parseDependencies(node: NodeSeq, queue: mutable.Queue[(NodeSeq, String)]): Unit =
      val dependencyNodes = node \\ "Dependency"
      val dependencyNodesAndPaths = dependencyNodes.map(depNode => (depNode, depNode \\ "@path"))
      for ((dep, path) <- dependencyNodesAndPaths) {
        queue.enqueue((dep, path.toString))
      }
    
    def isFakeDayFact(factPath: String): Boolean =
      if (dictionary.getDefinition(factPath) != null) return false
      
      val index = factPath.lastIndexOf("/")
      val updatedFactPath = factPath.dropRight(factPath.length - index)
      dictionary.getDefinition(updatedFactPath).typeNode == "DayNode" 


    var node = dictionary.getDefinitionsAsNodes().getOrElse(
      Path(originalPath).asAbstract,
      throw new Exception(s"Invalid path: $originalPath")
    )
    var debugString = node.toString

    val remainingPaths = mutable.Queue[(NodeSeq, String)]()
    parseDependencies(node, remainingPaths)

    // Expand dependencies
    while (remainingPaths.nonEmpty) {
      val currentPath = remainingPaths.dequeue()
      val factPath = transformFactPath(currentPath(1), originalPath)
      if (!isFakeDayFact(factPath) && !dictionary.getDefinition(factPath).isWritable && !Path(factPath).isWildcard) {
        val dependencyFactNode = dictionary.getDefinitionsAsNodes().getOrElse(
          Path(factPath).asAbstract,
          throw new Exception(s"Invalid path: ${factPath}")
        )
        parseDependencies(dependencyFactNode, remainingPaths)
        debugString = debugString.replace(currentPath(0).toString, dependencyFactNode.toString)
      }
    }

    // Convert string to xml and then update xml with appropriate values because formatting is otherwise preserved
    val sections = debugString.split("\n")
    val debugStringWithoutSpacing = StringBuilder()
    sections.foreach(section => debugStringWithoutSpacing.addAll(section.stripLeading()))
    val xmlString = debugStringWithoutSpacing.toString()

    val evts = Stream.emits(xmlString)
      .through(events[Fallible, Char]())
      .through(documents)
    val reconstructedXml = evts.compile.toList match {
      case Right(x) => x
      case Left(e)  => throw e
    }
    val printer = new PrettyPrinter(100, 2)

    val expandedFact: NodeSeq = reconstructedXml.head
    var expandedFactOutput = printer.format(expandedFact.head)
    val facts = expandedFact \\ "Fact"
    val uniqueFacts = facts.distinct
    uniqueFacts.foreach { factNode =>
      expandedFactOutput = prettifyFactWithValue(factNode, originalPath, expandedFactOutput)
    }

    val deps = expandedFact \\ "Dependency"
    val uniqueDeps = deps.distinct
    uniqueDeps.foreach { depNode =>
      expandedFactOutput = prettifyDependencyWithValue(depNode, originalPath, expandedFactOutput)
    }
    println(expandedFactOutput)

object Graph:
  def apply(dictionary: FactDictionary): Graph =
    this(dictionary, InMemoryPersister())

  def apply(dictionary: FactDictionary, persister: Persister): Graph =
    new Graph(dictionary, persister)
