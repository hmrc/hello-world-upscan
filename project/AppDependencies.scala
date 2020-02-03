import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(

    "uk.gov.hmrc"             %% "govuk-template"           % "5.31.0-play-25",
    "uk.gov.hmrc"             %% "play-ui"                  % "8.7.0-play-25",
    "uk.gov.hmrc"             %% "bootstrap-play-25"        % "5.1.0",
    "uk.gov.hmrc"             %% "simple-reactivemongo"     % "7.23.0-play-25"
  )

  val test = Seq(
    "org.scalatest"           %% "scalatest"                % "3.0.4"                 % "test",
    "org.jsoup"               %  "jsoup"                    % "1.10.2"                % "test",
    "com.typesafe.play"       %% "play-test"                % current                 % "test",
    "org.pegdown"             %  "pegdown"                  % "1.6.0"                 % "test, it",
    "uk.gov.hmrc"             %% "service-integration-test" % "0.2.0"                 % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "2.0.0"                 % "test, it",
    "uk.gov.hmrc"             %% "reactivemongo-test"       % "4.16.0-play-25"        % "test"
  )

}
