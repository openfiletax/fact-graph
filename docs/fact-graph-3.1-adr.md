# ADR: Fact Graph 3.1

- Created: [06/05/25]
- Published: [08/15/25]

## Primary author(s)
[primary authors]: #primary-authors

@petrosgov

## Problem Statement
[problem statement]: #problem-statement

The IRS is required to update its taxpayer-facing systems to implement major changes in tax law, and many IRS applications contain hard-coded, application-specific tax logic that is difficult and error-prone to update.
This is a problem that the Fact Graph, a standalone tax logic calculator that works with JVM and JavaScript-based applications, was invented to solve.

Unfortunately, the Fact Graph cannot currently be leveraged to support applications beyond its original consumer, Direct File.
The Fact Graph's APIs are tightly coupled with Direct File's APIs, and it is difficult to incorporate the Fact Graph without a lot of additional scaffolding.

Fact Graph 3.1 is an update of the current Fact Graph package to be a consumable library for IRS tax applications.
The update would provide a much simpler interface for interacting with the Fact Graph, while maintaining its type ergonomics and correctness guarantees.
The update would also make it easier to express scenarios that previously had to be augmented by application logic or convoluted fact patterns (i.e. `/writable*`).

Adoption of the Fact Graph would enable the IRS to respond to tax law changes with groundbreaking speed *and* precision.
It would make the IRS dramatically more efficient at delivering taxpayer-facing applications, by eliminating duplicative work to model the tax code across various software platforms and organizational boundaries.

## Prior Art
[prior art]: #prior-art

The existing Fact Graph, as implemented for Direct File in TS25, is called Fact Graph 3.0.
Unlike at least one previous iteration, it does not use YAML.

## Goals and Non-Goals
[goals and non-goals]: #goals-and-non-goals

### Goals
* Provide a library interface for the Fact Graph that requires no knowledge of its internals
* Establish 1p and 3p consumers of the library, ideally on both supported platforms (JVM and JS)
* Standardize Fact Dictionaries as a canonical format for declaratively modeling tax logic
* Decouple governance structure and release cadence of the library from that of its consuming applications

### Non-Goals
* Write additional tax logic - this is important, but downstream of FG3.1 itself
* Backwards compatibility with the Direct File 2025 application (`df-client`) - we will hold this as along as we can, but eventually it will likely need to be dropped

### Maybe-Goals
* Backwards compatibility with Direct File 2025 fact dictionary (at the very least it should be easy to provide a translator)

## Key terms or internal names

<dl>

  <dt>Fact Graph
  <dd>The entire project, including the library, its source code, and its documentation

  <dt>fact graph (lowercase)
  <dd>A particular instantiation of a user's tax scenario

  <dt>Fact Dictionary
  <dd>A set of facts about tax logic (i.e. to be eligible for X credit you must be Y years of age)

  <dt>Fact XML (FXML)
  <dd>The markup language used to define Fact Dictionaries

  <dt>TS25
  <dd>Tax Season 2025

</dl>

## Background & Motivation
[background and motivation]: #background--motivation

The Fact Graph is an engine for calculating taxes based on user-provided information.
It comprises the Fact Dictionary, a set of tax facts represented as XML documents, and the graph itself, which applies that logic to information provided by the taxpayer about their personal situation.
It was designed to support filing preparation for TS25;
in Direct File, each taxpayer's in-progress filing was represented as a Fact Graph.

The Fact Graph was a major advancement in the IRS' ability to declaratively model the tax code.
Tax logic represented as Fact Dictionaries could be easily updated, checked for correctness, and applied to in-progress tax returns.
It enabled the incredible accuracy that was a major contributor to Direct File's customer satisfaction.

## Suggested Solution
[design]: #suggested-solution

Barriers to socializing the Fact Graph as the best solution for modeling tax logic at the IRS are roughly threefold:

1. Packaging
1. Documentation
1. Semantics

### Packaging

The Fact Graph has a number of leaky interfaces that worked for Direct File but are unnecessarily cumbersome for a library.

#### Getting Started

First and foremost, consumers need to parse their own XML into Fact Dictionary primitives like [`WritableConfigTrait`](https://github.com/IRS-Public/direct-file/blob/e0d5c84451cc52b72d20d04652e306bf4af1a43c/direct-file/fact-graph-scala/shared/src/main/scala/gov/irs/factgraph/definitions/fact/WritableConfigTrait.scala) and [`LimitConfigTrait`](https://github.com/IRS-Public/direct-file/blob/e0d5c84451cc52b72d20d04652e306bf4af1a43c/direct-file/fact-graph-scala/shared/src/main/scala/gov/irs/factgraph/definitions/fact/LimitConfigTrait.scala).
In Direct File, this happens in [XmlProcessor.java](https://github.com/IRS-Public/direct-file/blob/e0d5c84451cc52b72d20d04652e306bf4af1a43c/direct-file/backend/src/main/java/gov/irs/directfile/api/loaders/processor/XmlProcessor.java) and [processFactsToDigestWrapper.js](https://github.com/IRS-Public/direct-file/blob/e0d5c84451cc52b72d20d04652e306bf4af1a43c/direct-file/df-client/df-client-app/src/fact-dictionary/generate-src/processFactsToDigestWrapper.ts).
This is difficult and unnecessary: the library should simply accept XML strings and return a fully functional FactDictionary object.

```js
import * as fg from './fact-graph.js'

const factDictionary = new fg.FactDictionary(xmlString)
const factGraph = new fg.FactGraph(factDictionary)
```

This makes it much easier for a consumer of the library to get started with a FactGraph object, which already has a great interface for setting facts, getting facts, and getting derived facts.

```js
factGraph.set('/age', 22)
factGraph.get('/age') // 22
factGraph.get('/is18OrOlder') // true
```

The Java interface should be essentially identical.

#### Collapsing interfaces

To better encapsulate its functionality, the Fact Graph needs a *much* simpler interface.
Many of these improvements are relatively low-hanging fruit: the parser constructs should be made private, factory patterns should be collapsed (i.e. [`FactDictionaryFactory`](./js/src/main/scala/gov/irs/factgraph/JSFactDictionary.scala) should just be a `FactDictionary`), and a variety of interfaces can instead by objects.

A slightly harder problem is the [persistence](shared/src/main/scala/gov/irs/factgraph/persisters/InMemoryPersister.scala) interface.
The desired API is quite simple: a fact graph instance should exist in-memory and offer a serialization method which returns its JSON representation;
The consumer of the library is responsible for when and how to persist this JSON.
This is how Direct File works in practice, and the API can be simplified to reflect that.

The main complication is that validation happens when a fact graph is saved.
This is an important ergonomic innovation in how fact graphs are constructed:
filling out tax information can be a long process, and the graph will necessarily be invalid at various points during the process.

Note also that not all invalid states are created equal.
For instance, an invalid address should never enter the fact graph, but it should be possible to continue progressing even if you are still missing some 1099 information;
the latter will need to be corrected at some point, but not necessarily now.

I expect that this can be solved by exposing validation through its own interface, rather than implicitly coupling it with `save()`.

### Documentation

The Fact Graph does not yet have the documentation required to be a standalone library.

First, the interface basics need to be written up.
This includes the constructors and methods on the `FactGraph` and `FactDictionary` objects.
Any additional classes and types that the library exports will also need documentation (another reason why it's imperative to plug the various leaky interfaces).

Second, the Fact Dictionary XML needs to be specified.
This would be one of the more important outcomes of this effort, because it would establish Fact Dictionaries as a standardized way to model legal logic.
The process of documenting the XML specification will also make it much clearer which semantic gaps still remain.

Finally, it would be nice to revive the Fact Graph [tutorials](./shared/src/main/scala/_tutorial) that currently sit inert in the repo.
These sheets make reference to a `FactDictionary.fromXml` method that was removed from the library because the native XML parsing module was JVM-only.
I added that method back, which should be most of the work of making the worksheets function again.
People interested in the Fact Graph benefit from an on-boarding walkthrough that introduces the concepts gently, with logic that's less complicated than the DF 2025 Tax Scope.

#### Types
The complexity of the Fact Graph's interface is multiplied by the need to provide types for every value primitive.
Making these types configurable in the Fact Dictionary would dramatically reduce the number of type primitives that Fact Graph users would need to be aware of.
I believe it is also possible to auto-generate `.d.ts` based on Fact Dictionaries, for JS consumers.

### Semantics

There are *ample* opportunities to make Fact XML (FXML) more expressive, many of which are necessary to decouple the logic engine from Direct File's requirements.

#### Dictionary-Defined Types

FXML needs declarable types.

Fact values are currently represented as a variety of primitive types.
Some of these types are very general, like `String`; others are very specific to IRS internals, like `IpPin`, which is just a string of a specific length, and `Address`, which has validation logic that is specific to MeF.
Socializing the Fact Graph to other consumers requires a method for altering these primitives without modifying the Fact Graph itself.

We should build a `<Types>` module in the `FactDictionary` that lets the dictionary author define new types based on the Fact Graph primitives.

Primitives:
* Boolean
* Integer
* Double (*not* float, for precision reasons)
* String
* Object (key-value pairs of other types)

Examples of dictionary-defined types:
* IpPin
* Address
* Tin

Slightly more advanced types that should maybe be primitives:
* Enum (different types of TIN, all strings)
* Enum Sum Type (a type that could be a string or an integer)

Possible syntax for type declaration:

```xml
<Type>
  <Name>IpPin</Name>
  <BaseType>String</BaseType>
  <Limit type="Match">
     <![CDATA[^([0-9]{6})$]]>
  </Limit>
</Type>
```

#### Dictionary-Defined Functions

When defining facts, we often repeat calculations. For instance, this is `f(d, b, t, p) = b + (d - t)p` in FXML:

```xml
<Add>
  <Dollar>2320</Dollar> <!-- b -->
  <Multiply>
    <Rational>12/100</Rational> <!-- p -->
    <Subtract>
      <Minuend>
        <Dependency path="/roundedTaxableIncome" /> <!-- d -->
      </Minuend>
      <Subtrahends>
        <Dollar>23200</Dollar> <!-- t -->
       </Subtrahends>
     </Subtract>
  </Multiply>
</Add>
```

FXML should provide a way to define re-usable calculations.

```xml
<DefineCalculation name="BasePlusPercentageOverThreshold">
  <Add>
    <Dollar><Parameter name="a"/></Dollar>
    <Multiply>
      <Rational><Parameter name="p"/></Rational>
      <Subtract>
        <Minuend><Parameter name="d"/></Minuend>
        <Subtrahends>
            <Dollar><Parameter name="t"/></Dollar>
         </Subtrahends>
       </Subtract>
    </Multiply>
  </Add>
</DefineCalculation>
```

This would then be available for derived facts.

```xml
<Fact name="/calc">
  <Derived>
    <Calculation name="BasePlusPercentageOverThreshold">
      <Var name="a">2320</Var>
      <Var name="p">12/100</Var>
      <Var name="d"><Dependency path="/roundedTaxableIncome" /></Var>
      <Var name="t">23200</Var>
    </Calculation>
  </Derived>
</Fact>
```

These names and concepts are not final.

#### Metadata

FXML should also have a `<Meta>` tag for arbitrary, user-defined metadata.

```xml
<Meta>
  <name>My Facts</name>
  <version>1.3.0</version>
</Meta>
```

This data should get serialized with the in-progress fact graph.

```json
{
  "facts": [],
  "meta": {
    "name": "My Facts",
    "version": "1.3.0"
  }
}
```

The above example demonstrates how this mechanism consolidates a couple unfinished ideas in the current Fact Graph: [metadata, versioning](./shared/src/main/scala/gov/irs/factgraph/Meta.scala), and [migrations](./shared/src/main/scala/gov/irs/factgraph/Migrations.scala).

I believe there should also be a space for library-defined configuration knobs (`<Config>`, probably), but that can be added later when it's more obvious what those knobs would be.

#### Optional Values

The Fact Dictionary has a Placeholder primitive but it does not have an Optional primitive.
It needs a way to model "the taxpayer might provide an answer to this, but leaving it blank is also fine."
The affirmative choice to leave a field blank (i.e. `Optional.Empty`) could also be used as a logical value by derived facts.

Fields that are optional should also have the ability to define a default.
For instance, `/dependents/*/suffixString` is an empty string when the taxpayer hasn't filled it out.
Right now we model this with the `/writable*` pattern and a `<Case>`.
This pattern is verbose, indirect, and unnecessary.

In general, a lot of the complexity of the TS25 Fact Dictionary could be removed by introducing constructs that eliminate the need for `/writable*` facts.

#### Paths and Wildcards

The concept of a fact path is entirely convention.
For instance: the fact `/filers/name` might be in a folder called `filers.xml`, but it could also be anywhere.

This is okay, but this pattern could be very easily extended to have proper modules, namespacing, and paths.
`<FactDictionaryModule path="filers">` could specify a root path, and then all the facts within that module would start with `/filers/`.

With formal paths, we could also have formal wildcards, instead of just a `*` that gets substituted.
The `*` is very convenient though, and I wouldn't want to add a formal notation for it if that notation weren't just as convenient (which I think it can be).

Wildcard values should also be accessible via indices (e.g. `/filers/[0]`) and iterators.

#### Implausibility

For a variety of compliance reasons, we are not allowed to prevent the taxpayer from entering extremely high values, even if those values would be ludicrous.
What we can do is say: it seems *unlikely* that this value is correct.

The Fact Graph could easily add a plausibility range, and provide a series of warnings on certain facts.
This mechanism could also be generalized as WARN validations vs ERROR validations.

#### Syntactic Sugar

The Fact Dictionary schema was designed to extremely explicit.
This often makes writing simple calculations tedious (i.e. `<Minuend>` and `<Subtrahend>`).
Although syntactic improvements are generally lower priority than semantic ones, it should be possible to make basic calculations less verbose.

# Timeline
[timeline]: #timeline

No fixed timeline, a lot of this work can take place in conjunction with FG3.1's adaption for other IRS projects.

# Dependencies
[dependencies]: #dependencies

None :)

# Alternatives Considered
[alternatives]: #alternatives-considered

## Do nothing
We could attempt to bring the Fact Graph as-is into a new application, but I think this would end up being a similar amount of work without the end benefit of re-usability.
You would still have to solve the problems of removing a lot of the DF-specific logic from the Fact Graph's API.

## Rewrite in TypeScript or Java

First and foremost, we do not have the resources to do this.

Second, the main reason that Scala was originally chosen still largely applies:
it is useful, at the IRS especially, to support both JS and JVM compile targets.
A rewrite in TypeScript or Java would require a lot of effort and produce a less-capable artifact.

If the Fact Graph were successfully deployed across multiple applications, and the Scala implementation became a barrier to velocity, there might be resources and a mandate to implement Fact XML in a new language.
As it stands, continuing the build on the existing Scala code is the only plausible path forward.

# Operations and Devops
[operations]: #operations-and-devops

FG3.1 should live in its own repository and do releases.
This is necessary both for convenience of consumption outside the TRUST team, and to better enforce and develop the library's interface boundaries.
This notably includes semantic versioning.

# Security/Privacy/Compliance
[security privacy compliance]: #security-privacy-compliance

The Fact Graph is an isolated, standalone library; updating it should not introduce any new variables in this regard.

# Risks
[risks]: #risks

## Complexity

It's possible that the existing fact graph is too complicated to be effectively updated and maintained by our current resource level.

If this is the case, however, we probably shouldn't be using the Fact Graph at all.

## Scala

Although Scala is a good fit for this particular problem (see "Alternatives Considered" section), it is a somewhat hipster language choice.
During DF development, modifying Scala was considered more difficult than modifying Java or JavaScript.
Continuing to work with Scala could be suboptimal when compared with an equivalent library written in Java or JavaScript.

In general, I believe that the difficulties DF had with modifying Scala code were largely due to the multiple layers of abstraction that existed between `df-client` and the Fact Graph.
Although many of those layers are removed by simply not using `df-client`, this ADR also improves the Fact Graph's interface, making it possible to use in the browser without scaffolding.
Put together, these improvements make it much easier to achieve the development loop of making a change in Scala and seeing its effects in the application.
This will, in turn, make the Scala itself more approachable.

# Revisions
[revisions]: #revisions

* 08/15/2025 - Initial publication
