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

import javax.inject.{Inject, Singleton}
import play.api.Logging
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, JsValidationException, NotFoundException, Upstream4xxResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.mobileselfassessment.cesa.{CesaAccountSummary, CesaFutureLiability, CesaInvalidDataException, CesaRootLinks, CesaRootLinksWrapper}
import uk.gov.hmrc.mobileselfassessment.config.AppConfig
import uk.gov.hmrc.mobileselfassessment.model.SaUtr

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CesaIndividualsConnector @Inject() (
  override val http: HttpClient,
  appConfig:         AppConfig
)(implicit ec:       ExecutionContext)
    extends PlayAuthConnector
    with Logging {

  lazy val serviceUrl: String = appConfig.cesaBaseUrl

  def url(path: String) = s"$serviceUrl$path"

  def getOptionalCesaAccountSummary(
    utr:               SaUtr,
    accountSummaryUrl: String
  )(implicit hc:       HeaderCarrier
  ): Future[Option[CesaAccountSummary]] =
    http.GET[Option[CesaAccountSummary]](url(accountSummaryUrl))

  def futureLiabilities(utr: SaUtr)(implicit hc: HeaderCarrier): Future[Seq[CesaFutureLiability]] = {
    val path = s"/self-assessment/individual/$utr/account/futureliabilities"
    http.GET[Option[Seq[CesaFutureLiability]]](url(path)) map {
      _.getOrElse(Seq.empty[CesaFutureLiability])
    }
  }

  def getRootLinks(utr: SaUtr)(implicit hc: HeaderCarrier): Future[CesaRootLinks] =
    http.GET[CesaRootLinksWrapper](url(s"/self-assessment/individual/$utr")).map(_.links).recover {
      case ex: Upstream4xxResponse if ex.upstreamResponseCode == 404 =>
        throw new NotFoundException(s"Unable to retrieve data: SA (Individual) root data for UTR '$utr'")
      case e: JsValidationException =>
        throw new CesaInvalidDataException(s"Unable to retrieve data: SA (Individual) root data for UTR '$utr'")
    }
}
