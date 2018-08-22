
organization in ThisBuild := "us.kibbs"
name := "circe-akka"
version := "0.1.0"

scalaVersion := "2.12.6"
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-Ypartial-unification",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused:implicits",
  "-Ywarn-unused:imports",
  "-Ywarn-unused:locals",
  "-Xfuture",
  "-opt:l:method"
)

scalacOptions in (Compile, console) ~= {
  _.filterNot(_.startsWith("-Ywarn-unused"))
}

scalacOptions in (Test, console) ~= {
  _.filterNot(_.startsWith("-Ywarn-unused"))
}

val akkaStreamVersion = "2.5.14"
val circeVersion = "0.9.3"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaStreamVersion % Test,
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "org.scalacheck" %% "scalacheck" % "1.14.0" % Test

)

licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
