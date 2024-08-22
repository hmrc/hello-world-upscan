/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.helloworldupscan.config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject()(configuration: Configuration,
                          servicesConfig: ServicesConfig):

  lazy val initiateUrl              = servicesConfig.baseUrl("upscan-initiate") + "/upscan/initiate"
  lazy val initiateV2Url            = servicesConfig.baseUrl("upscan-initiate") + "/upscan/v2/initiate"
  lazy val uploadRedirectTargetBase = loadConfig("upload-redirect-target-base")
  lazy val callbackEndpointTarget   = loadConfig("upscan.callback-endpoint")

  lazy val assetsPrefix   = loadConfig(s"assets.url") + loadConfig(s"assets.version")
  lazy val analyticsToken = loadConfig(s"google-analytics.token")
  lazy val analyticsHost  = loadConfig(s"google-analytics.host")

  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl   = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  private val contactHost                  = configuration.getOptional[String](s"contact-frontend.host").getOrElse("")
  private val contactFormServiceIdentifier = "MyService"

  private def loadConfig(key: String) =
    configuration.getOptional[String](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  val welshLanguageSupportEnabled: Boolean =
    configuration.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)
