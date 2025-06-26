package gov.irs.factgraph.persisters

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.*
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.compnodes.*
import gov.irs.factgraph.definitions.fact.*
import gov.irs.factgraph.types.*

import scala.annotation.unused
import java.util.UUID

class InMemoryPersisterSpec extends AnyFunSpec:
  def addFactConfig(pathString: String, typeName: String, dictionary: FactDictionary): FactDefinition =
    val config = FactConfigElement(pathString, Some(new WritableConfigElement(typeName)), None, None)
    FactDefinition.fromConfig(config)(using dictionary)

  def makeJsonData(path: String, typeName: String, value: String): String =
    s"""{"${path}":{"$$type":"gov.irs.factgraph.persisters.${typeName}Wrapper","item":${value}}}"""

  describe("InMemoryPersister") {
    val dictionary = FactDictionary()

    addFactConfig("/test", "Int", dictionary)

    val path = Path("/test")
    val klass = classOf[Int]

    describe(".getSavedResult") {
      describe("when the value is not defined") {
        val persister = InMemoryPersister()
        val graph = Graph(dictionary, persister)
        @unused
        val fact = graph(path)(0).get

        it("returns an incomplete result") {
          assert(persister.getSavedResult(path, klass) == Result.Incomplete)
        }
      }

      describe("when the value is defined") {
        val persister = InMemoryPersister()
        val graph = Graph(dictionary, persister)
        val fact = graph(path)(0).get
        given Factual = fact

        fact.value match
          case value: IntNode => value.set(42)

        persister.save()

        it("returns a complete result") {
          assert(persister.getSavedResult(path, klass) == Result.Complete(42))
        }
      }
    }

    describe(".setFact") {
      it("sets an undefined value") {
        val persister = InMemoryPersister()
        val graph = Graph(dictionary, persister)
        val fact = graph(path)(0).get
        given Factual = fact

        assert(persister.getSavedResult(path, klass) == Result.Incomplete)

        fact.value match
          case value: IntNode => value.set(42)

        assert(persister.getSavedResult(path, klass) == Result.Incomplete)

        persister.save()

        assert(persister.getSavedResult(path, klass) == Result.Complete(42))
      }

      it("updates a previously defined value") {
        val persister = InMemoryPersister()
        val graph = Graph(dictionary, persister)
        val fact = graph(path)(0).get
        given Factual = fact

        fact.value match
          case value: IntNode => value.set(42)

        persister.save()

        assert(persister.getSavedResult(path, klass) == Result.Complete(42))

        fact.value match
          case value: IntNode => value.set(0)

        persister.save()

        assert(persister.getSavedResult(path, klass) == Result.Complete(0))
      }
    }

    describe(".deleteFact") {
      it("deletes a previously defined value") {
        val persister = InMemoryPersister()
        val graph = Graph(dictionary, persister)
        val fact = graph(path)(0).get
        given Factual = fact

        fact.value match
          case value: IntNode => value.set(42)

        persister.save()

        assert(persister.getSavedResult(path, klass) == Result.Complete(42))

        fact.value.delete()

        assert(persister.getSavedResult(path, klass) == Result.Complete(42))

        persister.save()

        assert(persister.getSavedResult(path, klass) == Result.Incomplete)
      }

      describe("when the value is already not defined") {
        val persister = InMemoryPersister()
        val graph = Graph(dictionary, persister)
        val fact = graph(path)(0).get
        given Factual = fact
        assert(persister.getSavedResult(path, klass) == Result.Incomplete)

        it("does nothing") {
          fact.value.delete()
          persister.save()
          assert(persister.getSavedResult(path, klass) == Result.Incomplete)
        }
      }
    }
    describe(".toJson") {
      it("serializes its contents to JSON") {
        val persister = InMemoryPersister()
        val graph = Graph(dictionary, persister)
        val fact = graph(path)(0).get
        given Factual = fact

        fact.value match
          case value: IntNode => value.set(42)

        // This /meta/migrationsApplied value needs to be incremented when you add a new migration
        persister.save()
        assert(
          persister.toJson() == """{"/test":{"$type":"gov.irs.factgraph.persisters.IntWrapper","item":42},"/meta/migrationsApplied":{"$type":"gov.irs.factgraph.persisters.IntWrapper","item":2}}"""
        )
      }
    }
    describe("$apply(JsonString)") {
      it(
        "can re-hydrate a persister from a string of JSON that .toJson created",
      ) {
        val jsonData =
          """{"/test":{"$type":"gov.irs.factgraph.persisters.IntWrapper","item":42}}"""
        val persister = InMemoryPersister(jsonData)
        val graph = Graph(dictionary, persister)
        val fact = graph(path)(0).get
        given Factual = fact

        assert(persister.getSavedResult(path, klass) == Result.Complete(42))
      }
    }
    describe(".syncWithDictionary") {
      describe("when a persisted fact's type matches the dictionary") {
        it("indicates no problem") {
          val jsonData =
            """{"/test":{"$type":"gov.irs.factgraph.persisters.IntWrapper","item":42}}"""
          val persister = InMemoryPersister(jsonData)
          val graph = Graph(dictionary, persister)
          val result = persister.syncWithDictionary(graph)

          assert(result.isEmpty)
          val contents = persister.getSavedResult(Path("/test"), classOf[Any])
          assert(contents.complete)
          assert(contents.value.contains(42))
        }
      }

      describe("when a Int type value 1234 is persisted with path for a RationalNode") {
        it("removes the fact from the persister") {
          val pathString = "/test"
          val _dictionary = FactDictionary()
          addFactConfig(pathString, "Rational", _dictionary)

          val jsonData = makeJsonData(pathString, "Int", "123")
          val persister = InMemoryPersister(jsonData)
          val graph = Graph(_dictionary, persister)
          val result = persister.syncWithDictionary(graph)

          assert(result.length == 1)
          assert(result.head.path == pathString)
          val contents = persister.getSavedResult(Path(pathString), classOf[Any])
          assert(!contents.complete)
          assert(contents.value.isEmpty)
        }
      }

      describe("when a Boolean type value false is persisted with path for a String") {
        it("removes the fact from the persister") {
          val pathString = "/test"
          val _dictionary = FactDictionary()
          addFactConfig(pathString, "String", _dictionary)

          val jsonData = makeJsonData(pathString, "Boolean", "false")
          val persister = InMemoryPersister(jsonData)
          val graph = Graph(_dictionary, persister)
          val result = persister.syncWithDictionary(graph)

          assert(result.length == 1)
          assert(result.head.path == pathString)
          val contents = persister.getSavedResult(Path(pathString), classOf[Any])
          assert(!contents.complete)
          assert(contents.value.isEmpty)
        }
      }

      describe("when a String type value 12.34 is persisted with path for a Dollar") {
        it("removes the fact from the persister") {
          val pathString = "/test"
          val _dictionary = FactDictionary()
          addFactConfig(pathString, "Dollar", _dictionary)

          val jsonData = makeJsonData(pathString, "String", "\"12.34\"")
          val persister = InMemoryPersister(jsonData)
          val graph = Graph(_dictionary, persister)
          val result = persister.syncWithDictionary(graph)

          assert(result.length == 1)
          assert(result.head.path == pathString)
          val contents = persister.getSavedResult(Path(pathString), classOf[Any])
          assert(!contents.complete)
          assert(contents.value.isEmpty)
        }
      }

      describe("when a Dollar type value 12.34 is persisted with path for a String") {
        it("removes the fact from the persister") {
          val pathString = "/test"
          val _dictionary = FactDictionary()
          addFactConfig(pathString, "String", _dictionary)

          val jsonData = makeJsonData(pathString, "Dollar", "\"12.34\"")
          val persister = InMemoryPersister(jsonData)
          val graph = Graph(_dictionary, persister)
          val result = persister.syncWithDictionary(graph)

          assert(result.length == 1)
          assert(result.head.path == pathString)
          val contents = persister.getSavedResult(Path(pathString), classOf[Any])
          assert(!contents.complete)
          assert(contents.value.isEmpty)
        }
      }

      describe("when a persisted fact is not in the dictionary") {
        it("removes the fact from the persister") {
          val persistedPathString = "/somePersistedPath"
          val persistedPath = Path(persistedPathString)
          val jsonData =
            s"""{"${persistedPathString}":{"$$type":"gov.irs.factgraph.persisters.StringWrapper","item":"42"}}"""
          val persister = InMemoryPersister(jsonData)
          val graph = Graph(dictionary, persister)
          val result = persister.syncWithDictionary(graph)

          assert(result.length == 1)
          assert(result(0).path == persistedPathString)
          assert(
            persister
              .getSavedResult(persistedPath, classOf[Int])
              .complete == false,
          )
        }
      }
      describe("when a persisted fact is a derived fact in the dictionary") {
        it("removes the fact from the persister") {
          val pathString = "/test"
          val _dictionary = FactDictionary()

          FactDefinition.fromConfig(
            FactConfigElement(
              pathString,
              None,
              Some(
                CompNodeConfigElement(
                  "Int",
                  Seq.empty,
                  CommonOptionConfigTraits.value("42"),
                ),
              ),
              None,
            ),
          )(using _dictionary)

          val jsonData =
            s"""{"${pathString}":{"$$type":"gov.irs.factgraph.persisters.IntWrapper","item":"42"}}"""
          val persister = InMemoryPersister(jsonData)
          val graph = Graph(_dictionary, persister)
          val result = persister.syncWithDictionary(graph)

          assert(result.length == 1)
          assert(result(0).path == pathString)
          val contents =
            persister.getSavedResult(Path(pathString), classOf[Int])
          assert(contents.complete == false)
          assert(contents.value == None)
        }
      }
      describe("when a persisted fact is a collection member ... ") {
        val _dictionary = FactDictionary()
        val collection = FactDefinition.fromConfig(
          FactConfigElement(
            "/collection",
            Some(
              new WritableConfigElement("Collection"),
            ),
            None,
            None,
          ),
        )(using _dictionary)

        FactDefinition.fromConfig(
          FactConfigElement(
            "/collection/*/test",
            Some(new WritableConfigElement("Collection")),
            None,
            None,
          ),
        )(using _dictionary)

        FactDefinition.fromConfig(
          FactConfigElement(
            "/collection/*/test/*/sub",
            Some(new WritableConfigElement("Int")),
            None,
            None,
          ),
        )(using _dictionary)

        describe("... and the collection is in the dictionary") {
          val idString1 = "6b1259fd-8cdb-4efe-bcc8-ad40e604c98b"
          val idString2 = "81ac069b-7115-4aab-9fb3-f1bee92a0ebf"

          val pathString = s"/collection/#${idString1}/test"
          val pathString2 = s"${pathString}/#${idString2}/sub"

          it("indicates no problem for a valid item ID") {
            val jsonData =
              s"""{"${pathString2}":{"$$type":"gov.irs.factgraph.persisters.IntWrapper","item":"42"}}"""
            val persister = InMemoryPersister(jsonData)
            val graph = Graph(_dictionary, persister)
            val uuid1: UUID = UUID.fromString(idString1);
            val uuid2: UUID = UUID.fromString(idString2);

            for {
              result <- graph(Path("/collection"))
              fact <- result
            } fact.set(Collection(Vector(uuid1)))

            graph.save()

            for {
              result <- graph(Path(pathString))
              fact <- result
            } fact.set(Collection(Vector(uuid2)))

            graph.save()

            val result = persister.syncWithDictionary(graph)

            assert(result.isEmpty)
            val contents =
              persister.getSavedResult(Path(pathString), classOf[Any])
            assert(contents.complete)
            val contents2 =
              persister.getSavedResult(Path(pathString2), classOf[Any])
            assert(contents2.complete)
            assert(contents2.value.contains(42))
          }
          it("removes the fact for an invalid item ID") {
            val badIdString = "eb05d589-8933-4dee-a522-be61ffd026be"
            val badPathString = s"${pathString}/#${badIdString}/sub"
            val jsonData = s"""{
              "${badPathString}":{"$$type":"gov.irs.factgraph.persisters.IntWrapper","item":"42"},
              "${pathString2}":{"$$type":"gov.irs.factgraph.persisters.IntWrapper","item":"42"}
            }"""
            val persister = InMemoryPersister(jsonData)
            val graph = Graph(_dictionary, persister)
            val uuid1: UUID = UUID.fromString(idString1)
            val uuid2: UUID = UUID.fromString(idString2)

            for {
              result <- graph(Path("/collection"))
              fact <- result
            } fact.set(Collection(Vector(uuid1)))

            graph.save()

            for {
              result <- graph(Path(pathString))
              fact <- result
            } fact.set(Collection(Vector(uuid2)))

            graph.save()

            val result = persister.syncWithDictionary(graph)

            assert(result.length == 1)
            assert(result(0).path == badPathString)
            val badContents =
              persister.getSavedResult(Path(badPathString), classOf[Any])
            assert(badContents.complete == false)
            assert(badContents.value == None)
            val contents =
              persister.getSavedResult(Path(pathString), classOf[Any])
            assert(contents.complete == true)
            val contents2 =
              persister.getSavedResult(Path(pathString2), classOf[Any])
            assert(contents2.complete == true)
            assert(contents2.value == Some(42))
          }
        }
        describe("... and the collection is not found in the dictionary") {
          val pathString =
            "/unknown_collection/#6b1259fd-8cdb-4efe-bcc8-ad40e604c98b/test"
          val jsonData = s"""{
            "${pathString}": {
              "$$type": "gov.irs.factgraph.persisters.IntWrapper",
              "item": 42
            }
          }"""

          val persister = InMemoryPersister(jsonData)
          val graph = Graph(_dictionary, persister)

          it("removes the fact from the persister") {
            val result = persister.syncWithDictionary(graph)

            assert(result.length == 1)
            assert(result(0).path == pathString)
            val contents =
              persister.getSavedResult(Path(pathString), classOf[Any])
            assert(contents.complete == false)
            assert(contents.value == None)
          }
        }
      }
      describe("when a persisted fact is an enum value ... ") {
        val optionsPath = "/options-path"
        val _dictionary = FactDictionary()
        val pathString = "/test"
        FactDefinition.fromConfig(
          FactConfigElement(
            optionsPath,
            None,
            Some(
              new CompNodeConfigElement(
                "EnumOptions",
                Seq(
                  new CompNodeConfigElement(
                    "String",
                    Seq.empty,
                    CommonOptionConfigTraits.value("A"),
                  ),
                  new CompNodeConfigElement(
                    "String",
                    Seq.empty,
                    CommonOptionConfigTraits.value("B"),
                  ),
                  new CompNodeConfigElement(
                    "String",
                    Seq.empty,
                    CommonOptionConfigTraits.value("C"),
                  ),
                ),
                Seq.empty,
              ),
            ),
            None,
          ),
        )(using _dictionary)
        FactDefinition.fromConfig(
          FactConfigElement(
            pathString,
            Some(
              new WritableConfigElement(
                "Enum",
                CommonOptionConfigTraits.optionsPath(optionsPath),
              ),
            ),
            None,
            None,
          ),
        )(using _dictionary)
        val value = "A"
        val jsonData = s"""{
            "${pathString}": {
              "$$type": "gov.irs.factgraph.persisters.EnumWrapper",
              "item": {
                "value": ["${value}"],
                "enumOptionsPath": "${optionsPath}"
              }
            }
          }"""

        val persister = InMemoryPersister(jsonData)
        val graph = Graph(_dictionary, persister)

        it("it can read the enum value") {
          val result = persister.syncWithDictionary(graph)

          val contents =
            persister.getSavedResult(Path(pathString), classOf[Any])
          assert(contents.complete == true)
          assert(contents.value == Some(Enum(Some(value), optionsPath)))
        }
      }
    }
  }
