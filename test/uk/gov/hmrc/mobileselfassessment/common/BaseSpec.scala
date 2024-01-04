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

package uk.gov.hmrc.mobileselfassessment.common

import akka.actor.ActorSystem
import eu.timepit.refined.auto._
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.DefaultAwaitTimeout
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mobileselfassessment.MobileSelfAssessmentTestData
import uk.gov.hmrc.mobileselfassessment.connectors.ShutteringConnector
import uk.gov.hmrc.mobileselfassessment.mocks.{AuthorisationMock, ShutteringMock}
import uk.gov.hmrc.mobileselfassessment.model.types.ModelTypes.JourneyId

import scala.concurrent.ExecutionContext

trait BaseSpec
    extends AnyWordSpec
    with MockFactory
    with Matchers
    with DefaultAwaitTimeout
    with AuthorisationMock
    with ShutteringMock
    with MobileSelfAssessmentTestData {
  implicit lazy val ec:     ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit lazy val hc:     HeaderCarrier    = HeaderCarrier()
  implicit lazy val system: ActorSystem      = ActorSystem()

  val journeyId:                        JourneyId           = "13345a9d-0958-4931-ae83-5a36e4ccd979"
  implicit val mockShutteringConnector: ShutteringConnector = mock[ShutteringConnector]
  implicit val mockAuthConnector:       AuthConnector       = mock[AuthConnector]
  val confidenceLevel:                  ConfidenceLevel     = ConfidenceLevel.L200

  val enrolments: Set[Enrolment] =
    Set(Enrolment("IR-SA", identifiers = Seq(EnrolmentIdentifier("UTR", "utr")), state = "Activated"))

  val authorisedResponse:             GrantAccess = ConfidenceLevel.L200 and Enrolments(enrolments)
  val authorisedLowCLResponse:        GrantAccess = ConfidenceLevel.L50 and Enrolments(enrolments)
  val authorisedNoEnrolmentsResponse: GrantAccess = ConfidenceLevel.L200 and Enrolments(Set.empty)
}
