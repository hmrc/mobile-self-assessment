/*
 * Copyright 2025 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, JsValidationException, NotFoundException, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.mobileselfassessment.cesa.{CesaAccountSummary, CesaFutureLiability, CesaInvalidDataException, CesaRootLinks, CesaRootLinksWrapper}
import uk.gov.hmrc.mobileselfassessment.config.AppConfig
import uk.gov.hmrc.mobileselfassessment.model.{HipResponse, SaUtr}
import play.api.libs.json.{JsError, JsSuccess}

import java.util.Base64
import com.google.common.base.Charsets
import uk.gov.hmrc.mobileselfassessment.hip.{HipExceptions, HipResponseError}

import java.time.{LocalDate, ZoneId}
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class HipConnector @Inject() (val http: HttpClientV2, appConfig: AppConfig)(implicit ec: ExecutionContext) extends Logging {

  lazy val serviceUrl: String = s"${appConfig.hipUrl}/as"
  def url(path: String) = s"$serviceUrl$path"

  val correlationId = UUID.randomUUID.toString

  val encodedAuthToken = Base64.getEncoder.encodeToString(
    s"${appConfig.hipClientId}:${appConfig.hipClientSecret}".getBytes(Charsets.UTF_8)
  )

  val headers = Seq(
    "Authorization" -> s"Basic $encodedAuthToken",
    "Content-Type"  -> "application/json",
    "CorrelationId" -> correlationId
  )

  def getSelfAssessmentLiabilitiesData(utr: SaUtr)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ) = {

    val currentDate = LocalDate.now(ZoneId.of("Europe/London"))

    val queryParameters = Seq(
      "dateFrom" -> currentDate.minusYears(7).toString,
      "dateTo"   -> currentDate.toString
    )

    val path = s"/self-assessment/account/$utr/liability-details"
    http
      .get(url"${url(path)}")
      .transform(_.withQueryStringParameters(queryParameters*))
      .setHeader(headers*)
      .execute[HttpResponse]
      .flatMap {
        case response if response.status == 200 =>
          response.json.validate[HipResponse] match {
            case JsSuccess(hipResponse, _) => Future.successful(hipResponse)
            case JsError(error) =>
              logger.warn(
                s"validation failed on success payload received from HIP with error: $error"
              )
              Future.failed(HipExceptions(s"Unable to retrieve data: SA (Individual) root data for UTR '$utr' via HIP"))
          }
        case response if response.status == 422 || response.status == 404 =>
          Future.failed(NotFoundException(s"SA (Individual) root data for UTR '$utr' not found via HIP"))

        case response if response.status == 401 || response.status == 429 =>
          Future.failed(NotFoundException(s"SA (Individual) root data for UTR '$utr' not found via HIP"))
        case response if response.status == (503 | 500) =>
          Future.failed(HipExceptions(s"****** HIP service not available ******"))
        case response =>
          response.json.validate[HipResponseError] match {
            case JsSuccess(hipErrorResponse: HipResponseError, _) =>
              val errorSummary = hipErrorResponse.response.failures
                .map(e => s"${e.`type`}: ${e.reason}")
                .mkString("; ")
              logger.warn(
                s"call to HIP failed with status ${response.status}. Errors: $errorSummary"
              )
              Future.failed(HipExceptions(s"Error from downstream systems"))
            case JsError(error) =>
              logger.warn(s"validation failed on the error received from HIP with error: $error")
              Future.failed(HipExceptions(s"Json Validation error"))
          }
      }

  }

}
