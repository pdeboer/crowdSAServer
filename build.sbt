name := "bachelor"

version := "1.0"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq( jdbc , anorm , cache , ws )

libraryDependencies += "org.apache.pdfbox" % "pdfbox" % "1.8.7"

//libraryDependencies += "pdeboer" % "pplib_2.11" % "0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "com.typesafe.play" %% "play-slick" % "0.8.1"
)

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.4",
  "org.joda" % "joda-convert" % "1.6",
  "com.github.tototoshi" %% "slick-joda-mapper" % "1.2.0"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

lazy val bachelor = (project in file(".")).enablePlugins(play.PlayScala)