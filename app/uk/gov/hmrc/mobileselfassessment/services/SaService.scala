/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.mobileselfassessment.services

import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._
import play.api.mvc.Results.{NotFound, Ok}
import play.api.mvc.Result
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.mobileselfassessment.cesa.CesaRootLinks
import uk.gov.hmrc.mobileselfassessment.connectors.CesaIndividualsConnector
import uk.gov.hmrc.mobileselfassessment.model.{AccountSummary, FutureLiability, GetLiabilitiesResponse, SaUtr}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SaService @Inject() (cesaConnector: CesaIndividualsConnector) extends Logging {

  def getAccountSummary(
    utr:         SaUtr
  )(implicit hc: HeaderCarrier,
    ec:          ExecutionContext
  ): Future[Option[AccountSummary]] =
    cesaConnector.getRootLinks(utr).flatMap {
      case CesaRootLinks(Some(accountSummaryUrl)) =>
        cesaConnector.getOptionalCesaAccountSummary(utr, accountSummaryUrl).map(_.map(_.toSaAccountSummary))
      case _ => Future successful None
    }

  def getFutureLiabilities(
    utr:         SaUtr
  )(implicit hc: HeaderCarrier,
    ec:          ExecutionContext
  ): Future[Option[Seq[FutureLiability]]] = {
    val liabilities: Future[Seq[FutureLiability]] =
      cesaConnector.futureLiabilities(utr).map(_.map(_.toSaFutureLiability))
    liabilities.flatMap(list => if (list.isEmpty) Future successful None else Future successful Some(list))
  }

  def getLiabilitiesResponse(
    utr:         SaUtr
  )(implicit hc: HeaderCarrier,
    ec:          ExecutionContext
  ): Future[Option[GetLiabilitiesResponse]] =
    for {
      accountSummary    <- getAccountSummary(utr)
      futureLiabilities <- getFutureLiabilities(utr)
    } yield {
      accountSummary.map(summary =>
        GetLiabilitiesResponse(accountSummary = summary, futureLiability = futureLiabilities)
      )
    }
}
