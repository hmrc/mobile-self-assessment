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

package uk.gov.hmrc.mobileselfassessment.controllers

import org.scalamock.handlers.CallHandler
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.mobileselfassessment.model.{GetLiabilitiesResponse, SaUtr, Shuttering}
import uk.gov.hmrc.mobileselfassessment.services.{SaHipService, SaService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mobileselfassessment.common.BaseSpec

import scala.concurrent.{ExecutionContext, Future}

class LiabilitiesControllerSpec extends BaseSpec {

  private val fakeRequest = FakeRequest("GET", "/").withHeaders("Accept" -> "application/vnd.hmrc.1.0+json")
  private val mockSaService: SaService = mock[SaService]
  private val mockSaHipService: SaHipService = mock[SaHipService]

  private val controller = new LiabilitiesController(mockAuthConnector,
                                                     200,
                                                     "http:///spread-cost-url",
                                                     true,
                                                     Helpers.stubControllerComponents(),
                                                     mockSaService,
                                                     mockSaHipService,
                                                     mockShutteringConnector
                                                    )
  private val liabilitiesResponse = Json.parse(getLiabilitiesResponse).as[GetLiabilitiesResponse]

  def mockGetHipLiabilities(f: Future[Option[GetLiabilitiesResponse]]) =
    (mockSaHipService
      .getLiabilitiesResponse(_: SaUtr)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(f)

  def shutteringDisabled(): CallHandler[Future[Shuttering]] = mockShutteringResponse(Shuttering(shuttered = false))
//
  "GET /liabilities" should {
    "return 200" in {
      mockAuthorisationGrantAccess(authorisedResponse)
      shutteringDisabled()
      mockGetHipLiabilities(Future successful Some(liabilitiesResponse))
      val result = controller.getLiabilities(SaUtr("utr"), journeyId)(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return NOT FOUND when no account info is found" in {
      mockAuthorisationGrantAccess(authorisedResponse)
      shutteringDisabled()
      mockGetHipLiabilities(Future successful None)
      val result = controller.getLiabilities(SaUtr("utr"), journeyId)(fakeRequest)
      status(result) shouldBe Status.NOT_FOUND
    }

    "return UNAUTHORIZED when confidence level is too low" in {
      mockAuthorisationGrantAccess(authorisedLowCLResponse)
      val result = controller.getLiabilities(SaUtr("utr"), journeyId)(fakeRequest)
      status(result) shouldBe Status.UNAUTHORIZED
    }

    "return FORBIDDEN for valid utr for authorised user but for a different utr" in {
      mockAuthorisationGrantAccess(authorisedResponse)

      val result = controller.getLiabilities(SaUtr("differentUtr"), journeyId)(fakeRequest)

      status(result) shouldBe Status.FORBIDDEN
    }

    "return UNAUTHORIZED when no UTR is found on account" in {
      mockAuthorisationGrantAccess(authorisedNoEnrolmentsResponse)
      val result = controller.getLiabilities(SaUtr("utr"), journeyId)(fakeRequest)
      status(result) shouldBe Status.UNAUTHORIZED
    }

  }
}
