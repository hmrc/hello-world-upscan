import sbt._

object AppDependencies {

  val bootstrapVersion = "9.18.0"
  val hmrcMongoVersion = "2.6.0"

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc"             %% "play-frontend-hmrc-play-30"  % "12.7.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-30"          % hmrcMongoVersion,
    "uk.gov.hmrc.objectstore" %% "object-store-client-play-30" % "2.4.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"      % bootstrapVersion % Test,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"     % hmrcMongoVersion % Test
  )
}
