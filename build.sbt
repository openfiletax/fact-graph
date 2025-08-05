import org.scalajs.linker.interface.OutputPatterns

val projectName = "factgraph"
val factGraphVersion = "3.1.0-SNAPSHOT"
val scala3Version = "3.3.6"

lazy val root = project
  .in(file("."))
  .aggregate(factGraph.js, factGraph.jvm)
  .settings(
    name := projectName,
    version := factGraphVersion,
    scalaVersion := scala3Version,
    organization := "gov.irs",
    publish / skip := true
  )

// without extra libraries the javascript built is around 400kb.
lazy val factGraph = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("."))
  .settings(
    name := projectName,
    version := factGraphVersion,
    scalaVersion := scala3Version,
    organization := "gov.irs",
    scalaJSLinkerConfig ~= {
      // TODO: https://github.com/IRSDigitalService/trust/pull/359
      _.withModuleKind(ModuleKind.ESModule)
      // We export as MJS so that the NodeJS will run the tests with ESModules
      // This could be changed to %s.js
      .withOutputPatterns(OutputPatterns.fromJSFile("%s.mjs"))
    },
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.19" % Test,
    libraryDependencies += "org.scala-lang.modules" %%% "scala-xml" % "2.4.0",
    libraryDependencies += "com.lihaoyi" %%% "upickle" % "4.2.1",
    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % "2.5.0",
    libraryDependencies += "co.fs2" %%% "fs2-core" % "3.12.0",
    libraryDependencies += "co.fs2" %%% "fs2-scodec" % "3.12.0",
    libraryDependencies += "org.gnieh" %%% "fs2-data-xml-scala" % "1.12.0",
    Test / testOptions += Tests.Argument("-oI")
  )
  .jvmSettings(
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided"
  )
  .jsSettings(
  )
