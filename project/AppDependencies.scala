import play.core.PlayVersion
import sbt._

object AppDependencies {

  val bootstrapVersion = "8.1.0"
  val hmrcMongoVersion = "1.6.0"

  val compile = Seq(
    "uk.gov.hmrc"             %% "play-frontend-hmrc-play-30"  % "8.1.0",
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-30"          % hmrcMongoVersion
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"      % bootstrapVersion        % Test,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"     % hmrcMongoVersion        % Test,
    "org.scalatestplus.play"  %% "scalatestplus-play"          % "5.1.0"                 % Test,
  )

}
