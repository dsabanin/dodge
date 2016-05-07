import com.lihaoyi.workbench.Plugin._

enablePlugins(ScalaJSPlugin)

workbenchSettings

name := "Dodge"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.8.2",
  "com.lihaoyi" %%% "scalatags" % "0.5.4"
)

bootSnippet := "skullbeware.Dodge().main();"

updateBrowsers <<= updateBrowsers.triggeredBy(fastOptJS in Compile)

