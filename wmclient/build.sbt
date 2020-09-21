scalaVersion := "2.13.1"

name := "wurfl-microservice-scala"
organization := "com.scientiamobile.wurflmicroservice"
version := "2.1.0"

// Dependencies: the wurfl microservice client for scala works as a wrapper for the java one, providing a Scala friendly
// access to API calls to scala developers

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
libraryDependencies += "com.scientiamobile.wurflmicroservice" % "wurfl-microservice" % "2.1.0"
libraryDependencies += "org.scalatest" %% "scalatest" %"3.1.0" % "test"
libraryDependencies += "org.testng" % "testng" %"5.14.10" % "test"
libraryDependencies += "junit" % "junit" %"4.8.1" % "test"