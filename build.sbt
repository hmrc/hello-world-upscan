import uk.gov.hmrc.SbtArtifactory

val appName = "hello-world-upscan"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(majorVersion := 0)
  .settings(SbtDistributablesPlugin.publishingSettings: _*)
  .settings(libraryDependencies  ++= AppDependencies.compile ++ AppDependencies.test)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(scalacOptions += "-target:jvm-1.8")
  .settings(scalaVersion := "2.12.10")
