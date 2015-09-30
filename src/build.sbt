lazy val root = (project in file(".")).
  settings(
    organization := "cz.cuni.mff.ksi.xrg.ldvm",
    version := "0.1.0",
    scalaVersion := "2.11.7"
  )

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.11" % "2.2.5" % Test,
  "org.apache.jena" % "jena" % "2.13.0" exclude("org.slf4j", "slf4j-log4j12"),
  "org.apache.jena" % "jena-arq" % "2.13.0" exclude("org.slf4j", "slf4j-log4j12"),
  "com.jsuereth" %% "scala-arm" % "1.4",
  "com.typesafe.akka" %% "akka-actor" % "2.3.14",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "org.slf4j" % "slf4j-simple" % "1.7.12"
)