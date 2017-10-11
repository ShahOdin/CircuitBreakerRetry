name := "CircuitBreakerRetry"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(

  "com.typesafe.akka" %% "akka-actor" % "2.5.4",
  "org.typelevel" %% "spire" % "0.14.1",

  "org.scalatest" %% "scalatest" % "3.0.0" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.5.4" % Test
)
