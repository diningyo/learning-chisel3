// See README.md for license details.

def scalacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    // If we're building with Scala > 2.11, enable the compile option
    //  switch to support our anonymous Bundle definitions:
    //  https://github.com/scala/bug/issues/10047
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 => Seq()
      case _ => Seq("-Xsource:2.11")
    }
  }
}

def javacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    // Scala 2.12 requires Java 8. We continue to generate
    //  Java 7 compatible code for Scala 2.11
    //  for compatibility with old clients.
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 =>
        Seq("-source", "1.7", "-target", "1.7")
      case _ =>
        Seq("-source", "1.8", "-target", "1.8")
    }
  }
}

name := "learning-chisel3"

version := "3.1.1"

scalaVersion := "2.11.12"

crossScalaVersions := Seq("2.11.12", "2.12.4")

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

// Provide a managed dependency on X if -DXVersion="" is supplied on the command line.
val defaultVersions = Map(
  "chisel3" -> "3.3.0-RC1",
  "chisel-iotesters" -> "1.3.0",
  "treadle" -> "1.1.0"
  )

libraryDependencies ++= Seq("chisel3","chisel-iotesters").map {
  dep: String => "edu.berkeley.cs" %% dep % sys.props.getOrElse(dep + "Version", defaultVersions(dep)) }

scalacOptions ++= scalacOptionsVersion(scalaVersion.value)

javacOptions ++= javacOptionsVersion(scalaVersion.value)

lazy val commonSettings = Seq (
  organization := "edu.berkeley.cs",
  version := "2.0",
  //  git.remoteRepo := "git@github.com:ucb-bar/riscv-sodor.git",
  autoAPIMappings := true,
  scalaVersion := "2.11.12",
  crossScalaVersions := Seq("2.11.12", "2.12.6"),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases")
  ),
  scalacOptions := Seq(
    "-deprecation",
    "-feature",
    "-language:reflectiveCalls") ++ scalacOptionsVersion(scalaVersion.value),
  javacOptions ++= javacOptionsVersion(scalaVersion.value),
  libraryDependencies ++= Seq("chisel3", "chisel-iotesters").map {
    dep: String => "edu.berkeley.cs" %% dep % sys.props.getOrElse(dep + "Version", defaultVersions(dep))
  },
  updateOptions := updateOptions.value.withLatestSnapshots(false),
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
)

lazy val chiselFlatSpec = (project in file("subprj/chisel-flat-spec")).
  settings(commonSettings: _*)

lazy val chiselFlatSpecWithArgs = (project in file("subprj/chisel-flat-spec-with-args")).
  settings(commonSettings: _*)

lazy val loadChiselMem = (project in file("subprj/load-chisel-mem")).
  settings(commonSettings: _*)

lazy val parallelTestExecution = (project in file("subprj/parallel-test-execution")).
  settings(commonSettings: _*)

lazy val treadleOrFirrtlSim = (project in file("subprj/treadle-or-firrtl-sim")).
  settings(commonSettings: _*)

lazy val xorShift = (project in file("subprj/xorshift")).
  settings(commonSettings: _*)

lazy val bundleAlias = (project in file("subprj/bundle-alias")).
  settings(commonSettings: _*)

lazy val uintAndSIntShift = (project in file("subprj/uint-and-sint-shift")).
  settings(commonSettings: _*)

lazy val bundleRegInit = (project in file("subprj/bundle-reg-init")).
  settings(commonSettings: _*)

lazy val memND = (project in file("subprj/mem-nd")).
  settings(commonSettings: _*)

lazy val memStrbWrite = (project in file("subprj/mem-strb-write")).
  settings(commonSettings: _*)

lazy val mulDiv = (project in file("subprj/mul-div")).
  settings(commonSettings: _*)

lazy val testCtrl = (project in file("subprj/test-ctrl")).
  settings(commonSettings: _*)

lazy val bareAPICall = (project in file("subprj/bare-api-call")).
  settings(commonSettings: _*)

lazy val utilQueue = (project in file("subprj/util-queue")).
  settings(commonSettings: _*)

lazy val chiselName = (project in file("subprj/chisel-name")).
  settings(commonSettings: _*)

lazy val trialNIC = (project in file("subprj/trial-nic")).
  settings(commonSettings: _*)

lazy val bugSurvey = (project in file("subprj/bug-survey")).
  settings(commonSettings: _*)

lazy val arbiterTest = (project in file("subprj/arbiter-test")).
  settings(commonSettings: _*)

lazy val simWDT = (project in file("subprj/sim-wdt")).
  settings(commonSettings: _*)

lazy val multiIOModule = (project in file("subprj/multi-io-module")).
  settings(commonSettings: _*)

lazy val blackboxCheck = (project in file("subprj/blackbox-check")).
  settings(commonSettings: _*)

lazy val chisel320 = (project in file("subprj/chisel-3.2.0")).
  settings(commonSettings: _*)

lazy val vecTest = (project in file("subprj/vec-test")).
  settings(commonSettings: _*)

lazy val betterAsyncReset = (project in file("subprj/better-async-reset")).
  settings(commonSettings: _*)

lazy val parameterizeSwitch = (project in file("subprj/parameterize-switch")).
  settings(commonSettings: _*)

lazy val queueCompare = (project in file("subprj/queue-compare")).
  settings(commonSettings: _*)