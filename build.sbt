name := "rdb2rdf"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.7"

resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "9.4-1203-jdbc42",
  "mysql" % "mysql-connector-java" % "5.1.36",
  "org.xerial" % "sqlite-jdbc" % "3.8.11.1",
  "com.h2database" % "h2" % "1.4.189",

  "org.scala-lang.modules" %% "scala-swing" % "2.0.0-M2",

  "ch.qos.logback" % "logback-classic" % "1.1.3",

  "com.github.jgraph" % "jgraphx" % "v3.4.0.0",

  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

mainClass in assembly := Some("rdb2rdf.ui.RDB2RDFApp")
