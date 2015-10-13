name := "rdb2rdf"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "9.4-1203-jdbc42",
  "mysql" % "mysql-connector-java" % "5.1.36",
  "org.xerial" % "sqlite-jdbc" % "3.8.11.1",
  "com.h2database" % "h2" % "1.4.189",

  "org.scala-lang.modules" %% "scala-swing" % "2.0.0-M2",

  "ch.qos.logback" % "logback-classic" % "1.1.3",

  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

unmanagedJars in Compile <<= baseDirectory map { base => (base / "lib" ** "*.jar").classpath }

mainClass in assembly := Some("rdb2rdf.ui.RDB2RDFApp")
