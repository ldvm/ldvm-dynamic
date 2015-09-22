name := """ldvm-dynamic"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "org.scalatest" % "scalatest_2.11" % "2.2.5" % Test,
  "org.apache.jena" % "jena" % "2.13.0",
  "org.apache.jena" % "jena-arq" % "2.13.0",
  "commons-io" % "commons-io" % "2.4",
  "com.jsuereth" %% "scala-arm" % "1.4"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator


fork in run := true