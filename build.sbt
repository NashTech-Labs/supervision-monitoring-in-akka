name := "play-with-akka"

version := "1.0"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.8"
val scalaTestVersion = "3.0.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor_2.11" % akkaVersion,
  "com.typesafe.akka" % "akka-testkit_2.11" % akkaVersion,
  "org.scalatest" % "scalatest_2.11" % scalaTestVersion
)

    