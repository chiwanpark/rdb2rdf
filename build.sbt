name := "rdb2rdf"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "9.4-1203-jdbc42",
  "mysql" % "mysql-connector-java" % "5.1.36",
  "org.xerial" % "sqlite-jdbc" % "3.8.11.1",
  "com.h2database" % "h2" % "1.4.189",

  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)
