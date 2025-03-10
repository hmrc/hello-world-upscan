import sbt._

object AppDependencies {

  val bootstrapVersion = "9.11.0"
  val hmrcMongoVersion = "2.5.0"

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc"             %% "play-frontend-hmrc-play-30"  % "11.11.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-30"          % hmrcMongoVersion
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"      % bootstrapVersion % Test,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"     % hmrcMongoVersion % Test
  )
}
