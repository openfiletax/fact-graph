# Fact Graph

## Legal Disclaimer: Public Repository Access

> **No Endorsement or Warranty**
>
> The Internal Revenue Service (IRS) does not endorse, maintain, or guarantee the accuracy, completeness, or functionality of the code in this repository.
> The IRS assumes no responsibility or liability for any use of the code by external parties, including individuals, developers, or organizations.
> This includes—but is not limited to—any tax consequences, computation errors, data loss, or other outcomes resulting from the use or modification of this code.
>
> Use of the code in this repository is at your own risk. Users of this repository are responsible for complying with any open source or third-party licenses.

## What is the Fact Graph?

The Fact Graph is a production-ready knowledge graph for modeling, among other things, the United States Internal Revenue Code and related tax law.
It can be used in JavaScript as well as any JVM language (Java, Kotlin, Scala, Clojure, etc.).

## Onboarding and Set Up
See [ONBOARDING.md](ONBOARDING.md) for environment/developer setup.

See [the Fact Graph 3.1 ADR](docs/fact-graph-3.1-adr.md) for more information about the fact graph and how it has been changed since early 2025
See [here](docs/from-3.0-to-3.1.md) for a brief description of changes between the older versions of the Fact Graph and the current v3.1 in this repository 

## Contributing
See [CONTRIBUTING.md](CONTRIBUTING.md) for details.

## Repository Update Frequency 

This repository is updated frequently. Development occurs in a private repository and approved changes to `main` are pushed to this repository in real-time.

## Useful documentation
* [ScalaTest](https://www.scalatest.org/) - the testing framework we use
* [scala-xml](https://www.scala-lang.org/api/2.12.19/scala-xml/scala/xml/) - the standard implementation of XML (don't be put off by the sparse-seeming API docs, the function definitions have very good examples)
