/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.customerprofile.connector

import play.api.Logger
import play.api.http.Status
import uk.gov.hmrc.customerprofile.config.{ServicesCircuitBreaker, WSHttp}
import uk.gov.hmrc.customerprofile.domain.{Paperless, Preference}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{NotFoundException, _}

import scala.concurrent.{ExecutionContext, Future}


sealed trait PreferencesStatus

case object PreferencesExists extends PreferencesStatus

case object PreferencesCreated extends PreferencesStatus

case object PreferencesFailure extends PreferencesStatus

trait EntityResolverConnector extends Status {
  this: ServicesCircuitBreaker =>

  import Paperless.formats

  val externalServiceName = "entity-resolver"

  def http: HttpGet with HttpPost with HttpPut

  def serviceUrl: String

  def url(path: String) = s"$serviceUrl$path"

  def getPreferences()(implicit headerCarrier: HeaderCarrier, ex : ExecutionContext): Future[Option[Preference]] =
    withCircuitBreaker(http.GET[Option[Preference]](url(s"/preferences")))
      .recover {
        case response: Upstream4xxResponse if response.upstreamResponseCode == GONE => None
        case e: NotFoundException => None
      }

  def paperlessSettings(paperless: Paperless)(implicit hc: HeaderCarrier, ex : ExecutionContext): Future[PreferencesStatus] =
    withCircuitBreaker(http.POST(url(s"/preferences/terms-and-conditions"), paperless)).map(_.status).map {
      case OK => PreferencesExists
      case CREATED => PreferencesCreated
      case _ =>
        Logger.warn("Failed to update paperless settings")
        PreferencesFailure
    }
}

object EntityResolverConnector extends EntityResolverConnector with ServicesConfig with ServicesCircuitBreaker {
  override val serviceUrl = baseUrl("entity-resolver")

  override def http = WSHttp
}