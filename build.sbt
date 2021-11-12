

lazy val microservice = Project("hello-world-upscan", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    majorVersion := 0,
    scalaVersion := "2.12.14",
    libraryDependencies  ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions += "-target:jvm-1.8"
  )
  .settings(SbtDistributablesPlugin.publishingSettings: _*)
  .settings(resolvers += Resolver.jcenterRepo)
