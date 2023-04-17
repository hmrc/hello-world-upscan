import play.core.PlayVersion
import sbt._

object AppDependencies {

  val bootstrapVersion = "7.11.0"
  val hmrcMongoVersion = "0.73.0"

  val compile = Seq(
    "uk.gov.hmrc"             %% "govuk-template"              % "5.80.0-play-28",
    "uk.gov.hmrc"             %% "play-ui"                     % "9.12.0-play-28",
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-28"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"          % hmrcMongoVersion
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"      % bootstrapVersion        % Test,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"     % hmrcMongoVersion        % Test,
    "org.scalatestplus.play"  %% "scalatestplus-play"          % "4.0.3"                 % Test,
    "com.typesafe.play"       %% "play-test"                   % PlayVersion.current     % Test,
    "org.pegdown"              % "pegdown"                     % "1.6.0"                 % Test,
  )

}
