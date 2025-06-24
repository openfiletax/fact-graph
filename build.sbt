import org.scalajs.linker.interface.OutputPatterns
val scala3Version = "3.3.3"

lazy val root = project
  .in(file("."))
  .aggregate(factGraph.js, factGraph.jvm)
  .settings(
    name := "fact-graph",
    version := "0.1.0-SNAPSHOT",
    organization := "gov.irs.factgraph",
    scalaVersion := scala3Version,
    publish := {},
    publishLocal := {},
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided",
    libraryDependencies += "com.lihaoyi" %% "upickle" % "3.1.0",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.15" % Test,
    Test / testOptions += Tests.Argument("-oI")
  )

// without extra libraries the javascript built is around 400kb.
lazy val factGraph = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("."))
  .settings(
    name := "fact-graph",
    version := "0.1.0-SNAPSHOT",
    organization := "gov.irs.factgraph",
    scalaVersion := scala3Version,
    scalaJSLinkerConfig ~= {
      // We output CommonJSModules, which scalajs will put through the closure compiler in
      // the `fullLinkJS` step. This outputs approximately 1mb of javascript, whereas the
      // ESModules are 4mb. I tried outputting ESModules assuming that our build
      // pipeline would eventually minify them with esbuild. However, esbuild turned
      // out to be really, really bad at minifying these specific modules. So we're stuck
      // on common js modules for now. 
      _.withModuleKind(ModuleKind.CommonJSModule)
    },
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided",
    // want to get rid of this eventually.
    // just don't have time to implement my own dates now!
    // it adds around 200kb to the built package.
    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % "2.5.0",
    libraryDependencies += "com.lihaoyi" %%% "upickle" % "3.1.0",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.15" % Test,
    Test / testOptions += Tests.Argument("-oI")
  )
  .jvmSettings(
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided"
  )
  .jsSettings(
  )
