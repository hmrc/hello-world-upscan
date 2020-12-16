import play.core.PlayVersion
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "govuk-template"           % "5.60.0-play-26",
    "uk.gov.hmrc"             %% "play-ui"                  % "8.18.0-play-26",
    "uk.gov.hmrc"             %% "bootstrap-play-26"        % "2.2.0",
    "uk.gov.hmrc"             %% "simple-reactivemongo"     % "7.31.0-play-26"
  )

  val test = Seq(
    "org.scalatest"           %% "scalatest"                % "3.0.8"                 % Test,
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "3.1.2"                 % Test,
    "com.typesafe.play"       %% "play-test"                % PlayVersion.current     % Test,
    "org.pegdown"              % "pegdown"                  % "1.6.0"                 % Test,
    "uk.gov.hmrc"             %% "reactivemongo-test"       % "4.22.0-play-26"        % Test
  )

}
