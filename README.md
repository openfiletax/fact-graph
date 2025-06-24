# Fact Graph Scala

This repo holds the Scala code that is compiled to Java and transpiled to JS. The project uses

- Java 21
- Scala 3
- ScalaJS to transpile to JS
- SBT

## Installation

- (this section needs some love)
- install coursier (will download, and run AdoptOpenJDK11)
- change to version 21 with `eval "$(cs java --jvm 21 --env)"`
- set up sbt using coursier
- install [metals](https://marketplace.visualstudio.com/items?itemName=scalameta.metals) extension for your IDE

## General Usage

**Compile** the main sources in `src/main/scala` using `sbt compile`

**Delete** all generated files in the target directory using `sbt clean`

To clean and compile, use a one liner: `sbt clean compile`

**Console Access** Start the scala interpereter `sbt console`

**Reload** reload the build definition (build.sbt) `sbt reload`

**Run (not currently a feature)** eventually, using `sbt run` will run the main class for the project

**Test** using `sbt test` or to test only a single file use `sbt testOnly tinSpec.scala`

### How to get your Scala changes to the client

#### Quickstart
From `/direct-file/direct-file/fact-graph-scala`

1. run `sbt` (to drop into the sbt shell)
2. Let's say you are working on the Tin component and you updated the TinSpec. It would be quicker to run the TinSpec test ONLY rather than all the tests. This can be accomplished by running `testOnly *TinSpec`. Place any test name after the wildcard (*) and all matches will run. E.g, `testOnly *PinSpec` would run both the PinSpec and the IpPinSpec.
3. run `test` to test everything
4. run `scalafmt` to format the source
5. run `compile`
6. run `clean`
7. run `fastOptJS` to transpile Scala to JS
8. run `exit` to leave the sbt shell
9. `cd` back to `df-client-app`
10. if the client is running in another terminal, kill it (CTRL+C)
11. run `npm run copy-transpiled-js` to get the transpiled files
12. run `docker-compose build` to get the latest backend scala changes (depending on the Scala change this could be optional)
13. run `npm run start` to start the client
14. Load the checklist ( to load the factgraph on the client)
15. use `debugFactGraphScala…` to test the new functionality (in the browser console). This is a key step to see if your Scala changes came through. E.g, if I needed to modify the IpPinFactory to allow all zeros to fail, I could do implement the change in Scala. Go through steps 1-10 above and then type `debugFactGraphScala.IpPinFactory('0000000')` in the browser console to see if this fails now (as it should). If it does, you can rest assured that your Scala changes have been transpiled and copied successfully to the factgraph on the client.
16. When adding new Scala changes to the client, go to step 8.
17. start client integration

#### Testing in VS Code

While Step 2 (above) allows us to test an individual file (some set of unit tests), what if we wanted to test just a single unit, ie, a single `describe` block or single `it` block? The following allows us to do that in VS Code:

1. Open VS code from the `fact-graph-scala` directory
2. Click on the Metals extension. You can install it from [here](https://marketplace.visualstudio.com/items?itemName=scalameta.metals)
3. Under BUILD COMMANDS - click on `Import build` in Metals
4. Under PACKAGES - click on `factGraphJVM-test` folder in the navigator
5. Under ONGOING COMPILATIONS - wait for compilations to complete
6. Open any \*Spec file that you would like to run a single unit test against. The green arrow should show up along the file's line numbers vertically.

#### Details

For full transpilation, run `sbt fullOptJS`. For faster transpilation, run `sbt fastOptJS`. This will create the transpiled files. Copy transpiled files (main.js\*) from this repo to your local `js-factgraph-scala` within the df-client. This can be done two ways:

- in df-client-app run `npm run copy-transpiled-js` OR
- in fact-graph-scala directory run:

```
cp js/target/scala-3.2.0/fact-graph-opt/main.js* ../df-client/js-factgraph-scala/src/
```

Generate the main.mjs and main.mjs.map files into `js/target/scala-3.2.0/fact-graph-opt`: `sbt fullOptJS`

Note: `mjs` file type is used here to support ES6 modules used in scala tests.
For more information on the sbt-dotty plugin, see the [scala3-example-project](https://github.com/scala/scala3-example-project/blob/main/README.md).

#### Debug the factgraph in JS

There are some global variables exposed to be run in the console, after the factgraph loads, to troubleshoot:

```
> debugFactGraph
> debugFactGraphMeta
> debugScalaFactGraphLib
```

### Generating an SBOM

To generate a Software Bill of Materials (SBOM), run `sbt makeBom`. It will be missing some dependencies
that we will have to merge in from our `manual-scala-sbom.xml` (see that file for more information).
