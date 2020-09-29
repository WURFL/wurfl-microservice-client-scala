name := "example"
description := "Example executable scala application to try WURFL Microservice device detection"
version := "1.0.0"

scalaVersion := "2.13.3"

// Uncomment the line below if you want to build this example using your local artifact repository
//resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

libraryDependencies += "com.scientiamobile.wurflmicroservice" % "wurfl-microservice-scala" % "2.1.0"
