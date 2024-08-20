import play.sbt.PlayImport.PlayKeys.playDefaultPort

lazy val microservice = Project("hello-world-upscan", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    majorVersion         := 0,
    scalaVersion         := "3.3.3",
    libraryDependencies  ++= AppDependencies.compile ++ AppDependencies.test,
    playDefaultPort      := 9001,
    scalacOptions        += "-Wconf:cat=unused-imports&src=html/.*:s",
    scalacOptions        += "-Wconf:src=routes/.*:s"
  )
  .settings(resolvers += Resolver.jcenterRepo)
