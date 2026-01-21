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
import uk.gov.hmrc.domain
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.mobileselfassessment.model.{CidPerson, GetLiabilitiesResponse, SaUtr, Shuttering}
import uk.gov.hmrc.mobileselfassessment.services.{SaHipService, SaService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mobileselfassessment.common.BaseSpec
import uk.gov.hmrc.mobileselfassessment.connectors.CitizenDetailsConnector

import scala.concurrent.{ExecutionContext, Future}

class LiabilitiesControllerSpec extends BaseSpec {

  private val fakeRequest = FakeRequest("GET", "/").withHeaders("Accept" -> "application/vnd.hmrc.1.0+json")
  private val mockSaService: SaService = mock[SaService]
  private val mockSaHipService: SaHipService = mock[SaHipService]
  private val mockCDConnector: CitizenDetailsConnector = mock[CitizenDetailsConnector]

  def createController(enableITSA: Boolean) = new LiabilitiesController(
    mockAuthConnector,
    mockCDConnector,
    200,
    enableITSA,
    "/cessation-mobile",
    Helpers.stubControllerComponents(),
    mockSaService,
    mockSaHipService,
    mockShutteringConnector
  )
  private val liabilitiesResponse = Json.parse(getLiabilitiesResponse).as[GetLiabilitiesResponse]

  def mockGetHipLiabilities(f: Future[Option[GetLiabilitiesResponse]]) =
    (mockSaHipService
      .getLiabilitiesResponse(_: SaUtr, _: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(f)

  def mockGetLiabilities(f: Future[Option[GetLiabilitiesResponse]]) =
    (mockSaService
      .getLiabilitiesResponse(_: SaUtr, _: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(f)

  def mockGetUtrByNino(f: Future[Option[domain.SaUtr]]) =
    (mockCDConnector
      .getUtrByNino(_: String)(_: HeaderCarrier))
      .expects(*, *)
      .returning(f)

  def shutteringDisabled(): CallHandler[Future[Shuttering]] = mockShutteringResponse(Shuttering(shuttered = false))

  "GET /liabilities" should {
    "return 200" when {
      "ITSA is enabled" when {
        "IR-SA only enrolmemnt is present" in {
          mockAuthorisationGrantAccess(authorisedSaOnlyResponse)
          shutteringDisabled()
          mockGetHipLiabilities(Future successful Some(liabilitiesResponse))
          val result = createController(true).getLiabilities(SaUtr("utr"), journeyId)(fakeRequest)
          status(result) shouldBe Status.OK
        }

        "Both IR-SA MTD enrolment are present" in {
          mockAuthorisationGrantAccess(authorisedAllResponse)
          shutteringDisabled()
          mockGetHipLiabilities(Future successful Some(liabilitiesResponse))
          val result = createController(true).getLiabilities(SaUtr("utr"), journeyId)(fakeRequest)
          status(result) shouldBe Status.OK
        }

        "Only MTD enrolment is present" in {
          mockAuthorisationGrantAccess(authorisedMTDOnlyResponse)
          mockGetUtrByNino(Future.successful(Some(domain.SaUtr("1097133333"))))
          shutteringDisabled()
          mockGetHipLiabilities(Future successful Some(liabilitiesResponse))
          val result = createController(true).getLiabilities(SaUtr("1097133333"), journeyId)(fakeRequest)
          status(result) shouldBe Status.OK
        }

      }

      "ITSA not enabled" in {
        mockAuthorisationGrantAccess(authorisedSaOnlyResponse)
        shutteringDisabled()
        mockGetLiabilities(Future successful Some(liabilitiesResponse))
        val result = createController(false).getLiabilities(SaUtr("utr"), journeyId)(fakeRequest)
        status(result) shouldBe Status.OK
      }

    }

    "return NOT FOUND when no account info is found" when {

      "ITSA is enabled" in {
        mockAuthorisationGrantAccess(authorisedSaOnlyResponse)
        shutteringDisabled()
        mockGetHipLiabilities(Future successful None)
        val result = createController(true).getLiabilities(SaUtr("utr"), journeyId)(fakeRequest)
        status(result) shouldBe Status.NOT_FOUND
      }

      "ITSA is not enabled" in {
        mockAuthorisationGrantAccess(authorisedSaOnlyResponse)
        shutteringDisabled()
        mockGetLiabilities(Future successful None)
        val result = createController(false).getLiabilities(SaUtr("utr"), journeyId)(fakeRequest)
        status(result) shouldBe Status.NOT_FOUND
      }

    }

    "return UNAUTHORIZED when confidence level is too low" in {
      mockAuthorisationGrantAccess(authorisedLowCLResponse)
      val result = createController(true).getLiabilities(SaUtr("utr"), journeyId)(fakeRequest)
      status(result) shouldBe Status.UNAUTHORIZED
    }

    "return FORBIDDEN for valid utr for authorised user but for a different utr" when {

      "SA only enrolment is present" in {
        mockAuthorisationGrantAccess(authorisedSaOnlyResponse)

        val result = createController(true).getLiabilities(SaUtr("differentUtr"), journeyId)(fakeRequest)

        status(result) shouldBe Status.FORBIDDEN
      }

      "Both SA and MTD enrolments are present" in {
        mockAuthorisationGrantAccess(authorisedAllResponse)

        val result = createController(true).getLiabilities(SaUtr("differentUtr"), journeyId)(fakeRequest)

        status(result) shouldBe Status.FORBIDDEN
      }

      "MTD only enrolment is present" in {
        mockAuthorisationGrantAccess(authorisedMTDOnlyResponse)
        mockGetUtrByNino(Future.successful(Some(domain.SaUtr("1097133333"))))
        val result = createController(true).getLiabilities(SaUtr("utr"), journeyId)(fakeRequest)
        status(result) shouldBe Status.FORBIDDEN
      }

    }

    "return UNAUTHORIZED when no UTR is found on account" when {
      "No enrolment is present" in {
        mockAuthorisationGrantAccess(authorisedNoEnrolmentsResponse)
        val result = createController(true).getLiabilities(SaUtr("utr"), journeyId)(fakeRequest)
        status(result) shouldBe Status.UNAUTHORIZED
      }

      "MTD-only enrolment is present and no utr found from cid call" in {
        mockAuthorisationGrantAccess(authorisedMTDOnlyResponse)
        mockGetUtrByNino(Future.successful(None))
        val result = createController(true).getLiabilities(SaUtr("utr"), journeyId)(fakeRequest)
        status(result) shouldBe Status.UNAUTHORIZED
      }

    }

  }
}
