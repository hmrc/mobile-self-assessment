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

package uk.gov.hmrc.mobileselfassessment.controllers

import org.joda.time.{LocalDate}
import org.scalamock.handlers.CallHandler
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.auth.core.{ConfidenceLevel, InsufficientConfidenceLevel}
import play.api.mvc.Result
import uk.gov.hmrc.mobileselfassessment.model.{GetLiabilitiesResponse, SaUtr, Shuttering}
import uk.gov.hmrc.mobileselfassessment.common.BaseSpec
import scala.concurrent.Future

class SandboxLiabilitiesControllerSpec extends BaseSpec {

  private val sut = new SandboxLiabilitiesController(
    mockAuthConnector,
    ConfidenceLevel.L200.level,
    Helpers.stubControllerComponents(),
    mockShutteringConnector
  )

  "GET /sandbox/liabilities" should {
    "return 200" in {
      mockAuthorisationGrantAccess(confidenceLevel)
      shutteringDisabled()

      val request = FakeRequest("GET", "/sandbox/liabilities")
        .withHeaders("Accept" -> "application/vnd.hmrc.1.0+json")

      val result: Future[Result] = sut.getLiabilities(SaUtr("utr"), journeyId)(request)
      status(result) shouldBe 200
      val response: GetLiabilitiesResponse = contentAsJson(result).as[GetLiabilitiesResponse]
      response.accountSummary.taxToPayStatus.toString                                   shouldBe "OverdueWithBill"
      response.accountSummary.totalAmountDueToHmrc.amount                               shouldBe 12345.67
      response.accountSummary.totalAmountDueToHmrc.requiresPayment                      shouldBe true
      response.accountSummary.amountHmrcOwe                                             shouldBe 0
      response.futureLiability.get.head.futureLiabilities.head.descriptionCode.toString shouldBe "BCD"
      response.futureLiability.get.head.futureLiabilities.head.dueDate.toString         shouldBe "2015-01-31"
      response.futureLiability.get.head.futureLiabilities.head.amount                   shouldBe 503.2
      response.futureLiability.get.head.futureLiabilities.head.taxYear.start            shouldBe 2014
      response.futureLiability.get.head.futureLiabilities.head.taxYear.end              shouldBe 2015
      response.futureLiability.get.head.futureLiabilities.last.descriptionCode.toString shouldBe "IN1"
      response.futureLiability.get.head.futureLiabilities.last.partnershipReference.get shouldBe SaUtr("1097172564")
      response.futureLiability.get.head.futureLiabilities.last.dueDate.toString         shouldBe "2015-01-31"
      response.futureLiability.get.head.futureLiabilities.last.amount                   shouldBe 2300
      response.futureLiability.get.head.futureLiabilities.last.taxYear.start            shouldBe 2014
      response.futureLiability.get.head.futureLiabilities.last.taxYear.end              shouldBe 2015
    }
  }

  "GET /sandbox/liabilities" should {
    "return 401" in {
      mockAuthorisationFailure(InsufficientConfidenceLevel())

      val request = FakeRequest("GET", "/sandbox/liabilities")
        .withHeaders("Accept" -> "application/vnd.hmrc.1.0+json")
      val result = sut.getLiabilities(SaUtr("utr"), journeyId)(request)
      status(result) shouldBe 401
    }
  }

  "return 521 when shuttered" in {
    mockAuthorisationGrantAccess(confidenceLevel)
    shutteringEnabled()

    val request = FakeRequest("GET", "/sandbox/liabilities")
      .withHeaders("Accept" -> "application/vnd.hmrc.1.0+json")

    val result: Future[Result] = sut.getLiabilities(SaUtr("utr"), journeyId)(request)
    status(result) shouldBe 521
  }

  private def shutteringDisabled(): CallHandler[Future[Shuttering]] =
    mockShutteringResponse(Shuttering(shuttered = false))

  private def shutteringEnabled(): CallHandler[Future[Shuttering]] =
    mockShutteringResponse(Shuttering(shuttered = true))
}
