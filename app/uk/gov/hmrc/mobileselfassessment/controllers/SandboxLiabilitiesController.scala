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

import play.api.Logger
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.*
import uk.gov.hmrc.api.controllers.HeaderValidator
import uk.gov.hmrc.api.sandbox.FileResource
import uk.gov.hmrc.mobileselfassessment.model.{GetLiabilitiesResponse, SaUtr}
import uk.gov.hmrc.mobileselfassessment.model.types.JourneyId
import uk.gov.hmrc.mobileselfassessment.connectors.ShutteringConnector
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class SandboxLiabilitiesController @Inject() (
  cc: ControllerComponents,
  shutteringConnector: ShutteringConnector
)(implicit override val executionContext: ExecutionContext)
    extends BackendController(cc)
    with ControllerChecks
    with HeaderValidator
    with FileResource {

  override def parser: BodyParser[AnyContent] = controllerComponents.parsers.anyContent

  override val logger: Logger = Logger(this.getClass)

  def getLiabilities(
    utr: SaUtr,
    journeyId: JourneyId
  ): Action[AnyContent] =
    validateAccept(acceptHeaderValidationRules).async { implicit request =>
      shutteringConnector.getShutteringStatus(journeyId).flatMap { shuttered =>
        withShuttering(shuttered) {
          Future successful Ok(readData(resource = "sandbox-liabilities-response.json"))
        }
      }
    }

  private def readData(resource: String): JsValue =
    toJson(
      Json
        .parse(
          findResource(s"/resources/mobileselfassessment/$resource")
            .getOrElse(throw new IllegalArgumentException("Resource not found!"))
            .replace("<FUTURE_DUE_DATE>", LocalDate.now().plusMonths(6).toString)
            .replace("<DAYS_REMAINING>", ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.now().plusMonths(6)).toString)
        )
        .as[GetLiabilitiesResponse]
    )
}
