name		:= "spark-streaming-dds"

version		:= "0.0.1-SNAPSHOT"

organization 	:= "org.apache.spark"

scalaVersion 	:= "2.11.5"

resolvers += "Vortex Snapshot Repo" at "https://dl.dropboxusercontent.com/u/19238968/vortex/mvn-repo"

resolvers += "nuvo.io maven repo" at "http://nuvo-io.github.com/mvn-repo/snapshots"

libraryDependencies += "com.prismtech.cafe" % "cafe" % "2.1.0p1-SNAPSHOT"

libraryDependencies += "io.nuvo" % "moliere_2.11" % "0.6.1-SNAPSHOT"

libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "1.2.0"

libraryDependencies += "org.apache.spark" % "spark-streaming_2.11" % "1.2.0"

autoCompilerPlugins := true

scalacOptions ++= Seq(
  "-Xelide-below", "MINIMUM",
  "-Xdev",
  "-optimise",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-feature",
  "-Yinline-warnings",
  "-Xlint:_"
)

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath + "/xlab/nuvo/mvn-repo/snapshots")))