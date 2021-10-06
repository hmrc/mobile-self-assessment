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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, BodyParser, ControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.mobileselfassessment.controllers.action.AccessControl
import uk.gov.hmrc.mobileselfassessment.model.SaUtr
import uk.gov.hmrc.mobileselfassessment.model.types.ModelTypes.JourneyId
import uk.gov.hmrc.mobileselfassessment.services.ShutteringService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source

@Singleton()
class SandboxLiabilitiesController @Inject() (
                                        override val authConnector:                                   AuthConnector,
                                        @Named("controllers.confidenceLevel") override val confLevel: Int,
                                        cc:                                                           ControllerComponents,
                                        shutteringService:                                            ShutteringService
                                      )(implicit override val executionContext:                                ExecutionContext)
  extends BackendController(cc)
    with ControllerChecks
    with AccessControl {

  override def parser: BodyParser[AnyContent] = controllerComponents.parsers.anyContent

  def getLiabilities(utr: SaUtr, journeyId: JourneyId): Action[AnyContent] =
    validateAcceptWithAuth(acceptHeaderValidationRules).async { implicit request =>
      shutteringService.getShutteringStatus(journeyId).flatMap { shuttered =>
        withShuttering(shuttered) {
          Future.successful(Ok(Json.toJson(sampleJson)))
        }
      }
    }

  private def sampleJson: JsValue = {
    val source = Source.fromFile("app/uk/gov/hmrc/mobileselfassessment/resources/sandbox-liabilities-response.json")
    val raw    = source.getLines.mkString
    source.close()
    Json.parse(raw)
  }
}
