enablePlugins(JavaAppPackaging)

lazy val buildSettings = Seq(
  organization := "com.digitalocean",
  scalaVersion := "2.11.8"
)

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-language:existentials",
  "-Xlint",
  "-language:implicitConversions",
  "-Yinline-warnings",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-value-discard",
  "-Xfuture"
)

lazy val specs2Version = "3.8"

lazy val testDependencies = Seq(
  "org.specs2"      %%  "specs2-core"       %   specs2Version,
  "org.specs2"      %%  "specs2-scalacheck" %   specs2Version,
  "org.specs2"      %%  "specs2-junit"      %   specs2Version,
  "org.specs2"      %%  "specs2-mock"       %   specs2Version 
)

scalacOptions in Test ++= Seq("-Yrangepos")

lazy val baseSettings = Seq(
  scalacOptions ++= compilerOptions, 
  scalacOptions in (Compile, console) := compilerOptions, 
  libraryDependencies ++= testDependencies.map(_ % "test"),
  autoAPIMappings := true
)

lazy val allSettings  = buildSettings ++ baseSettings 

lazy val packageTree = project.in(file("."))
  .settings(moduleName := "root")
  .settings(allSettings)
  .settings(
    libraryDependencies ++= Seq(
    )
  )
