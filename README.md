# Fact Graph

A knowledge graph for modeling the United States Internal Revenue Code, and other logic.
It can be used in JavaScript as well as any JVM language (Java, Kotlin, Scala, Clojure, etc.).

User documentation is forthcoming, and will be linked here;
the remainder of this README covers installing and modifying the code.

## Installation

The Fact Graph is a Scala project, so it requires Scala, a JDK, and sbt (scala build tool).
On MacOS, you can install these with Homebrew:

```sh
brew install scala openjdk sbt
```

Depending on how much Java stuff you already have on your local development machine, you may need to ensure that sbt is able to find the JDK.

```sh
brew link --force openjdk
export JAVA_HOME=$(brew --prefix openjdk)
```

A NodeJS installation is also required to run the test suite against the JS build.

## General Usage

* `sbt compile` - compile the JVM build
* `sbt fastOptJS` - compile the JS build quickly
* `sbt fullOptJS` - compile the JS build for production
* `sbt clean` - delete the build artifacts
* `sbt test` - run the tests (both Java and JS)
* `sbt testOnly tinSpec.scala` - run just the `tinSpec.scala` test
* `sbt publishLocal` - publish to your local repository to use Fact Graph with other projects

Some commands depend on other commands.
For instance, `sbt test` will run `sbt fastOptJS` to compile the JS build, if necessary.

Like maven, sbt allows you to chain commands.
To run the tests from a completely fresh build, you can run `sbt clean test`

## IDE Integration

### IntelliJ

IntelliJ has a [scala plugin](https://www.jetbrains.com/help/idea/get-started-with-scala.html).
Install it, ensure that IntelliJ can find the JDK you installed, and no additional setup should be necessary.

### VS Code

1. Install the [Metals extension](https://marketplace.visualstudio.com/items?itemName=scalameta.metals).
2. Open VSCode in the source root and click on the newly-installed Metals extensions
3. Under BUILD COMMANDS, click on `Import build`
4. Under PACKAGES, click on `factGraphJVM-test` folder in the navigator
5. Wait for compilations to complete
6. Open any \*Spec file that you would like to run a single unit test against. The green arrow should show up along the file's line numbers vertically.

### NeoVim

NeoVim also has a [Metals extension](https://github.com/scalameta/nvim-metals).

## Worksheets

In the [\_tutorial](./shared/src/main/scala/_tutorial) directory is a set of worksheets that teach the basics of the Fact Graph.
With the [appropriate setup](https://docs.scala-lang.org/scala3/book/tools-worksheets.html), IDEs can run these worksheets and display their output alongside them, like a Jupyter notebook.

I was able to get this working in VSCode but not IntelliJ (IntelliJ had trouble importing the package.)

## Debug the factgraph in JS

There are some global variables exposed to be run in the console, after the factgraph loads, to troubleshoot:

```
> debugFactGraph
> debugFactGraphMeta
> debugScalaFactGraphLib
```

## Useful documentation

* [ScalaTest](https://www.scalatest.org/) - the testing framework we use
* [scala-xml](https://www.scala-lang.org/api/2.12.19/scala-xml/scala/xml/) - the standard implementation of XML (don't be put off by the sparse-seeming API docs, the function definitions have very good examples)
