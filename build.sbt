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
  "chisel3" -> "3.1.+",
  "chisel-iotesters" -> "[1.2.5,1.3-SNAPSHOT["
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
  scalacOptions := Seq("-deprecation", "-feature") ++ scalacOptionsVersion(scalaVersion.value),
  javacOptions ++= javacOptionsVersion(scalaVersion.value),
  libraryDependencies ++= (Seq("chisel3", "chisel-iotesters").map {
    dep: String => "edu.berkeley.cs" %% dep % sys.props.getOrElse(dep + "Version", defaultVersions(dep))
  })
)

lazy val chiselFlatSpec = (project in file("subprj/chisel-flat-spec")).
  settings(commonSettings: _*)

lazy val chiselFlatSpecWithArgs = (project in file("subprj/chisel-flat-spec-with-args")).
  settings(commonSettings: _*)

lazy val loadChiselMem = (project in file("subprj/load-chisel-mem")).
  settings(commonSettings: _*)

lazy val treadleOrFirrtlSim = (project in file("subprj/treadle-or-firrtl-sim")).
  settings(commonSettings: _*)

lazy val xorShift = (project in file("subprj/xorshift")).
  settings(commonSettings: _*)

lazy val bundleAlias = (project in file("subprj/bundle-alias")).
  settings(commonSettings: _*)

lazy val uintAndSIntShift = (project in file("subprj/uint-and-sint-shift")).
  settings(commonSettings: _*)