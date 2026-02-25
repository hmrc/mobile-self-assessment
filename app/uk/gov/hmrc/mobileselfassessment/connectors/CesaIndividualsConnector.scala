/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.mobileselfassessment.connectors

import com.google.inject.name.Named

import javax.inject.{Inject, Singleton}
import com.google.common.base.Charsets
import play.api.Logging
import play.api.http.MimeTypes
import uk.gov.hmrc.http.{HeaderCarrier, JsValidationException, NotFoundException, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.mobileselfassessment.cesa.{CesaAccountSummary, CesaFutureLiability, CesaInvalidDataException, CesaRootLinks, CesaRootLinksWrapper}
import uk.gov.hmrc.mobileselfassessment.config.AppConfig
import uk.gov.hmrc.mobileselfassessment.model.SaUtr

import java.util.{Base64, UUID}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CesaIndividualsConnector @Inject() (
  val http: HttpClientV2,
  appConfig: AppConfig,
  @Named("enableDWIT") val enableDWIT: Boolean = false
)(implicit ec: ExecutionContext)
    extends Logging {

  lazy val serviceUrl: String = appConfig.cesaBaseUrl

  def url(path: String) = s"$serviceUrl$path"

  val correlationId = UUID.randomUUID.toString

  lazy val encodedAuthToken = Base64.getEncoder.encodeToString(
    s"${appConfig.hipCesaClientId}:${appConfig.hipCesaClientSecret}".getBytes(Charsets.UTF_8)
  )

  val originatorId: String = appConfig.hipCesaOriginatorId

  val headers = Seq(
    play.api.http.HeaderNames.CONTENT_TYPE -> MimeTypes.JSON,
    "Gov-Uk-Originator-Id"                 -> originatorId,
    "Authorization"                        -> s"Basic $encodedAuthToken",
    "CorrelationId"                        -> correlationId
  )

  def getOptionalCesaAccountSummary(
    utr: SaUtr,
    accountSummaryUrl: String
  )(implicit hc: HeaderCarrier): Future[Option[CesaAccountSummary]] = {
    if (enableDWIT) {
      http
        .get(url"${url(accountSummaryUrl)}")
        .setHeader(headers*)
        .execute[Option[CesaAccountSummary]]
    } else {
      http
        .get(url"${url(accountSummaryUrl)}")
        .execute[Option[CesaAccountSummary]]
    }

  }

  def futureLiabilities(utr: SaUtr)(implicit hc: HeaderCarrier): Future[Seq[CesaFutureLiability]] = {
    val path =
      if (enableDWIT) s"/ods-sa/v1/self-assessment/individual/$utr/account/futureliabilities"
      else s"/self-assessment/individual/$utr/account/futureliabilities"
    if (enableDWIT) {
      http
        .get(url"${url(path)}")
        .setHeader(headers*)
        .execute[Option[Seq[CesaFutureLiability]]]
        .map {
          _.getOrElse(Seq.empty[CesaFutureLiability])
        }
    } else {
      http
        .get(url"${url(path)}")
        .execute[Option[Seq[CesaFutureLiability]]]
        .map {
          _.getOrElse(Seq.empty[CesaFutureLiability])
        }
    }

  }

  def getRootLinks(utr: SaUtr)(implicit hc: HeaderCarrier): Future[CesaRootLinks] = {
    val path = if (enableDWIT) s"/ods-sa/v1/self-assessment/individual/$utr" else s"/self-assessment/individual/$utr"
    if (enableDWIT) {
      http
        .get(url"${url(path)}")
        .setHeader(headers*)
        .execute[CesaRootLinksWrapper]
        .map(_.links)
        .recover {
          case ex: UpstreamErrorResponse if ex.statusCode == 404 =>
            throw new NotFoundException(s"Unable to retrieve data: SA (Individual) root data for UTR '$utr'")
          case e: JsValidationException =>
            throw new CesaInvalidDataException(s"Unable to retrieve data: SA (Individual) root data for UTR '$utr'")
        }
    } else {
      http
        .get(url"${url(path)}")
        .execute[CesaRootLinksWrapper]
        .map(_.links)
        .recover {
          case ex: UpstreamErrorResponse if ex.statusCode == 404 =>
            throw new NotFoundException(s"Unable to retrieve data: SA (Individual) root data for UTR '$utr'")
          case e: JsValidationException =>
            throw new CesaInvalidDataException(s"Unable to retrieve data: SA (Individual) root data for UTR '$utr'")
        }
    }

  }
}
