import Dependencies._

ThisBuild / scalaVersion := "2.13.5"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.term-chat"
ThisBuild / organizationName := "term-chat"

lazy val root = (project in file("."))
  .settings(
    name := "term-chat",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )

ThisBuild / scalafixDependencies += "com.nequissimus" %% "sort-imports" % "0.5.5"

val zioVersion = "1.0.7"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-streams" % zioVersion,
  "io.d11" %% "zhttp" % "1.0.0.0-RC15",
  "io.github.kitlangton" %% "zio-magic" % "0.1.12",
  "dev.zio" %% "zio-test" % zioVersion % "test",
  "dev.zio" %% "zio-test-sbt" % zioVersion % "test"
)

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))

scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-deprecation",
  "-unchecked",
  "-language:higherKinds",
  "-Wdead-code",
  "-Wunused:privates",
  "-Wunused:locals",
  "-Wunused:explicits",
  "-Wunused:params",
  "-Xlint:unused",
  "-Wconf:cat=unused:info",
  "-Ymacro-annotations",
  "-language:postfixOps"
)
