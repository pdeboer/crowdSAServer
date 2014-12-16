name := "bachelor"

version := "1.0"

lazy val `bachelor` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq( jdbc , anorm , cache , ws )

libraryDependencies += "org.apache.pdfbox" % "pdfbox" % "1.8.7"

libraryDependencies += "pdeboer" % "pplib_2.11" % "0.1-SNAPSHOT"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  