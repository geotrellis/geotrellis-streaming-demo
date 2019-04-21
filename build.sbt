lazy val commonSettings = Seq(
  version := "0.0.1-SNAPSHOT",
  scalaVersion := Version.scalaVersion,
  crossScalaVersions := Version.crossScalaVersions,
  organization := "com.azavea",
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:higherKinds",
    "-language:postfixOps",
    "-language:existentials",
    "-feature"
  ),

  outputStrategy := Some(StdoutOutput),

  addCompilerPlugin("org.spire-math" % "kind-projector" % Version.kindProjector cross CrossVersion.binary),
  addCompilerPlugin("org.scalamacros" %% "paradise" % Version.macroParadise cross CrossVersion.full),

  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
    Resolver.bintrayRepo("azavea", "maven"),
    Resolver.bintrayRepo("azavea", "geotrellis")
  ),

  fork := true,
  Test / fork := true,
  Test / parallelExecution := false,
  javaOptions ++= Seq(s"-Djava.library.path=${Environment.ldLibraryPath}"),

  test in assembly := {},

  /** Shapeless shading due to circe */
  assemblyShadeRules in assembly := {
    val shadePackage = "com.azavea.shaded"
    Seq(
      ShadeRule.rename("shapeless.**" -> s"$shadePackage.shapeless.@1").inAll,
      ShadeRule.rename("javax.ws.rs.**" -> s"$shadePackage.javax.ws.rs.@1").inAll
    )
  },
  assemblyMergeStrategy in assembly := {
    case "reference.conf" => MergeStrategy.concat
    case "application.conf" => MergeStrategy.concat
    case n if n.endsWith(".SF") || n.endsWith(".RSA") || n.endsWith(".DSA") => MergeStrategy.discard
    case "META-INF/MANIFEST.MF" => MergeStrategy.discard
    case _ => MergeStrategy.first
  },

  licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
  headerLicense := Some(HeaderLicense.ALv2("2019", "Azavea"))
)

lazy val root =
  Project("geotrellis-streaming-demo", file("."))
    .aggregate(producer, streaming)
    .settings(commonSettings: _*)

lazy val producer =
  (project in file("producer"))
    .dependsOn(streaming)
    .settings(commonSettings: _*)
    .settings(libraryDependencies += Dependencies.scalaTest % Test)

lazy val streaming =
  (project in file("streaming"))
    .settings(commonSettings: _*)
    .settings(libraryDependencies ++= Seq(
      Dependencies.geotrellisContrib,
      Dependencies.circeCore,
      Dependencies.circeGeneric,
      Dependencies.circeGenericExtras,
      Dependencies.circeParser,
      Dependencies.sparkCore,
      Dependencies.sparkStreaming,
      Dependencies.sparkStreamingKafka,
      Dependencies.decline,
      Dependencies.scalaTest % Test
    ))
