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

import com.google.inject.name.Named
import play.api.Logger
import play.api.libs.json.Json
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.mvc.{Action, AnyContent, BodyParser, ControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}
import uk.gov.hmrc.mobileselfassessment.connectors.ShutteringConnector
import uk.gov.hmrc.mobileselfassessment.model.SaUtr
import uk.gov.hmrc.mobileselfassessment.services.SaService
import uk.gov.hmrc.mobileselfassessment.controllers.action.AccessControl
import uk.gov.hmrc.mobileselfassessment.model.types.ModelTypes.JourneyId
import uk.gov.hmrc.play.http.HeaderCarrierConverter.fromRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class LiabilitiesController @Inject() (
  override val authConnector:                                   AuthConnector,
  @Named("controllers.confidenceLevel") override val confLevel: Int,
  cc:                                                           ControllerComponents,
  saService:                                                    SaService,
  shutteringConnector:                                          ShutteringConnector
)(implicit val executionContext:                                ExecutionContext)
    extends BackendController(cc)
    with AccessControl
    with ControllerChecks
    with ErrorHandling {

  override def parser: BodyParser[AnyContent] = controllerComponents.parsers.anyContent
  override val app:    String                 = "Liabilities-Controller"
  override val logger: Logger                 = Logger(this.getClass)

  def getLiabilities(
    utr:       SaUtr,
    journeyId: JourneyId
  ): Action[AnyContent] = validateAcceptWithAuth(acceptHeaderValidationRules).async { implicit request =>
    implicit val hc: HeaderCarrier = fromRequest(request).withExtraHeaders(HeaderNames.xSessionId -> journeyId.value)
    shutteringConnector.getShutteringStatus(journeyId).flatMap { shuttered =>
      withShuttering(shuttered) {
        errorWrapper {
          saService.getLiabilitiesResponse(utr).map {
            case None           => NotFound
            case Some(response) => Ok(Json.toJson(response))
          }
        }
      }
    }
  }
}
