/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.mobileselfassessment.mocks

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mobileselfassessment.cesa.{CesaAccountSummary, CesaFutureLiability, CesaRootLinks}
import uk.gov.hmrc.mobileselfassessment.connectors.CesaIndividualsConnector
import uk.gov.hmrc.mobileselfassessment.model.SaUtr

import scala.concurrent.Future

trait CesaMock extends MockFactory {

  protected def mockGetRootLinks(
    response:               Future[CesaRootLinks]
  )(implicit cesaConnector: CesaIndividualsConnector
  ): CallHandler[Future[CesaRootLinks]] =
    (cesaConnector
      .getRootLinks(_: SaUtr)(_: HeaderCarrier))
      .expects(*, *)
      .returning(response)

  protected def mockGetOptionalCesaAccountSummary(
    response:               Option[CesaAccountSummary]
  )(implicit cesaConnector: CesaIndividualsConnector
  ): CallHandler[Future[Option[CesaAccountSummary]]] =
    (cesaConnector
      .getOptionalCesaAccountSummary(_: SaUtr, _: String)(_: HeaderCarrier))
      .expects(*, *, *)
      .returning(Future successful response)

  protected def mockGetFutureLiabilities(
    response:               Seq[CesaFutureLiability]
  )(implicit cesaConnector: CesaIndividualsConnector
  ): CallHandler[Future[Seq[CesaFutureLiability]]] =
    (cesaConnector
      .futureLiabilities(_: SaUtr)(_: HeaderCarrier))
      .expects(*, *)
      .returning(Future successful response)

}
