import gov.irs.factgraph.{Factual, FactDictionary, Graph, Path}
import gov.irs.factgraph.compnodes.CompNode
import gov.irs.factgraph.persisters.InMemoryPersister
import gov.irs.factgraph.types.{Collection, CollectionItem}
import java.util.UUID

// ##################################
// ##  4. Collections and Vectors  ##
// ##################################
//
// Taxes frequently feature sets of repeated items. A tax return may include
// multiple filers, dependents, W-2s, 1099s, etc. We will call these sets of
// repeated items Collections. Let's use dependents as an example.

val dictionary = FactDictionary.fromXml(
  <Dictionary>
    <Fact path="/taxYear">
      <Derived><Int>2021</Int></Derived>
    </Fact>

    <Fact path="/familyAndHousehold">
      <Writable><Collection /></Writable>
    </Fact>

    <Fact path="/familyAndHousehold/*/yearOfBirth">
      <Writable><Int /></Writable>
    </Fact>

    <Fact path="/familyAndHousehold/*/age">
      <Derived>
        <Subtract>
          <Minuend><Dependency path="/taxYear" /></Minuend>
          <Subtrahends><Dependency path="../yearOfBirth" /></Subtrahends>
        </Subtract>
      </Derived>
    </Fact>

    // We're going to play with this dictionary quite a lot. We'll use CompNodes
    // that pretend to be this entryPoint fact in order to avoid needing to
    // redefine the dictionary repeatedly.
    <Fact path="/entryPoint">
      <Derived><True /></Derived>
    </Fact>
  </Dictionary>
)

val dependentId1 = UUID.randomUUID()
val dependentId2 = UUID.randomUUID()
val dependentId3 = UUID.randomUUID()

val graph = Graph(
  dictionary,
  InMemoryPersister(
    "/familyAndHousehold" -> Collection(
      Vector(dependentId1, dependentId2, dependentId3)
    ),
    s"/familyAndHousehold/#$dependentId1/yearOfBirth" -> 2013,
    s"/familyAndHousehold/#$dependentId2/yearOfBirth" -> 2016,
    s"/familyAndHousehold/#$dependentId3/yearOfBirth" -> 2018
  )
)

// A Collection is represented as just another Fact, although it has some
// special properties. Let's take a look.

graph.get("/familyAndHousehold")

// As with any Fact, it returns a Result, reflecting that the Collection could
// be incomplete. The value contains a Vector with three UUIDs, representing
// each of the collection items, in this case, dependents. We can access a
// CollectionItem fact using the UUIDs.

graph.get(s"/familyAndHousehold/#$dependentId1")

// Note that we did not define this child fact; it was created automatically by
// a feature called extracts. Essentially, we know that a Collection fact will
// always have CollectionItem children, so the Fact Graph takes care of them for
// us. Date facts also use this feature to create year, month, and day extracts.
//
// TODO: Date HASN'T BEEN IMPLEMENTED YET
//
// A CollectionItem contains one or more facts, which are repeated across all of
// the items in the Collection.

graph.get(s"/familyAndHousehold/#$dependentId1/age")

// Looking closer at the definition of age, we can see something interesting
// about how it describes its arguments. The dependents' year of birth uses a
// relative path...

<Dependency path="../yearOfBirth" />

// ...indicating that the year of birth is specific to that particular
// dependent. Meanwhile the tax year is an absolute path...

<Dependency path="/taxYear" />

// ...reflecting that the tax year is shared across the entire tax return. We
// could also have written tax year as a relative path like so...

<Dependency path="../../../taxYear" />

// ...but this is a lot more cumbersome and unnecessary when dealing with a
// single fact located at the root of the graph.
//
// We can use a wildcard (*) to get all of the children of a collection at once.

graph.getVect("/familyAndHousehold/*")

// We can also use this to get access all of the dependents' ages.

graph.getVect(s"/familyAndHousehold/*/age")

// Note that instead of using graph.get, we have used graph.getVect. If we look
// at the return type, we'll see that we now have a MaybeVector of Results. A
// MaybeVector can either be Single or Multiple.
//
// If the MaybeVector is Single, it will always contain just one Result. Indeed,
// if you check out the implementation of .get in Graph.scala, you will see that
// .get is just a convenience method that gives us the output of getVect if and
// only if that output is Single.
//
// If the MaybeVector is Multiple, it contains any number of Results, including
// zero. And just like the Result type, a Multiple MaybeVector has completeness,
// reflecting whether the number of items in the Collection could change.

graph.get("/familyAndHousehold").complete
graph.getVect("/familyAndHousehold/*").complete
graph.getVect(s"/familyAndHousehold/*/age").complete

// Because the dependents Collection Fact is marked as complete, all of the
// MaybeVectors of its children are also complete. If the Collection was
// incomplete, say because the taxpayer was still in the process of adding
// dependents, the MaybeVectors would be incomplete, letting us know that they
// might still change.
//
// A Fact can use a wildcard in its definition. This fact will always return
// multiple values:

given Factual = graph(Path("/entryPoint"))(0).get

CompNode
  .fromXml(
    <Dependency path="/familyAndHousehold/*/age" />
  )
  .get

// The definition will always be a Multiple, and must be accessed using
// graph.getVect. More often, however, we'll aggregate these multiple values
// using operations like Count and Sum. Here's an example.

CompNode
  .fromXml(
    <Count>
      <LessThanOrEqual>
        <Left><Dependency path="/familyAndHousehold/*/age" /></Left>
        <Right><Int>6</Int></Right>
      </LessThanOrEqual>
    </Count>
  )
  .get

// This fact counts the number of dependents who are six or younger. Now the
// fact returns a Single, reflecting that however many dependents there are,
// they will always be aggregated to a single count.
//
// Note that Single MaybeVectors don't have completeness. This makes sense
// because they will always contain a single Result. But did we lose information
// when aggregating the Multiple ages?
//
// No! If the Multiple MaybeVector had been incomplete, then the aggregated
// Result would have been incomplete too, reflecting that if more dependents are
// added, the count of those six and younger might change.
//
// Also note that before aggregating, we compared the Multiple to the value of
// six. But <Int>6</Int> is Single! How did this work?
//
// From this, we can learn that all Fact Graph operations are _vectorized_. When
// one of the arguments to an operation is Single, and another is Multiple, the
// output will be a Multiple, calculated by applying the Single value to each of
// the Multiple values in turn.
//
// We can even operate on multiple Multiple arguments, provided that they all
// come from the same Collection and thus have the same length. For example,
// take this useless fact that doubles the ages and adds one.

CompNode
  .fromXml(
    <Add>
      <Dependency path="/familyAndHousehold/*/age" />
      <Dependency path="/familyAndHousehold/*/age" />
      <Int>1</Int>
    </Add>
  )
  .get

// As long as all of the arguments are either Multiples of the same length or
// Single, we can go wild combining them and let the Fact Graph do its thing.
//
// So if Collections and CollectionItems are Facts, then does that mean we could
// operate on them like any other Facts? In short, yes, and the possibilities
// that arise are described in the next chapter.
