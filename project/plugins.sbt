addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.5")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.13.2")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.2")
// This plugin is currently the only sbom builder for scala
// but even then, it looks like it has limited usage.
// If this plugin ever gives us issues, we should
// be open to re-evaluating it.
addSbtPlugin("io.github.siculo" %% "sbt-bom" % "0.3.0")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")