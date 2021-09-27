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

package uk.gov.hmrc.mobileselfassessment.controllers

import mocks.{AuthorisationMock, ShutteringMock}
import org.scalamock.handlers.CallHandler
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalamock.scalatest.MockFactory
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.auth.core.{AuthConnector, ConfidenceLevel}
import uk.gov.hmrc.mobileselfassessment.connectors.ShutteringConnector
import uk.gov.hmrc.mobileselfassessment.model.{GetLiabilitiesResponse, SaUtr, Shuttering}
import uk.gov.hmrc.mobileselfassessment.services.SaService
import eu.timepit.refined.auto._
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.mobileselfassessment.MobileSelfAssessmentTestData
import uk.gov.hmrc.mobileselfassessment.cesa.CesaRootLinks
import uk.gov.hmrc.mobileselfassessment.model.types.ModelTypes.JourneyId

import scala.concurrent.{ExecutionContext, Future}

class LiabilitiesControllerSpec
    extends AnyWordSpec
    with Matchers
    with MockFactory
    with AuthorisationMock
    with ShutteringMock
    with MobileSelfAssessmentTestData {

  private val fakeRequest = FakeRequest("GET", "/").withHeaders("Accept" -> "application/vnd.hmrc.1.0+json")
  implicit lazy val ec:                 ExecutionContext    = scala.concurrent.ExecutionContext.Implicits.global
  implicit val mockAuthConnector:       AuthConnector       = mock[AuthConnector]
  implicit val mockShutteringConnector: ShutteringConnector = mock[ShutteringConnector]
  private val mockSaService:            SaService           = mock[SaService]
  private val journeyId:                JourneyId           = "13345a9d-0958-4931-ae83-5a36e4ccd979"

  val confidenceLevel: ConfidenceLevel = ConfidenceLevel.L200

  private val controller = new LiabilitiesController(mockAuthConnector,
                                                     200,
                                                     Helpers.stubControllerComponents(),
                                                     mockSaService,
                                                     mockShutteringConnector)
  private val liabilitiesResponse = Json.parse(getLiabilitiesResponse).as[GetLiabilitiesResponse]

  def mockGetLiabilities(f: Future[Option[GetLiabilitiesResponse]]) =
    (mockSaService
      .getLiabilitiesResponse(_: SaUtr)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *)
      .returning(f)

  def shutteringDisabled(): CallHandler[Future[Shuttering]] = mockShutteringResponse(Shuttering(shuttered = false))

  "GET /liabilities" should {
    "return 200" in {
      mockAuthorisationGrantAccess(confidenceLevel)
      shutteringDisabled()
      mockGetLiabilities(Future successful Some(liabilitiesResponse))
      val result = controller.getLiabilities(SaUtr("utr"), journeyId)(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return NOT FOUND when no account info is found" in {
      mockAuthorisationGrantAccess(confidenceLevel)
      shutteringDisabled()
      mockGetLiabilities(Future successful None)
      val result = controller.getLiabilities(SaUtr("utr"), journeyId)(fakeRequest)
      status(result) shouldBe Status.NOT_FOUND
    }

    "return UNAUTHORIZED when confidence leve is too low" in {
      mockAuthorisationGrantAccess(ConfidenceLevel.L50)
      val result = controller.getLiabilities(SaUtr("utr"), journeyId)(fakeRequest)
      status(result) shouldBe Status.UNAUTHORIZED
    }

  }
}
