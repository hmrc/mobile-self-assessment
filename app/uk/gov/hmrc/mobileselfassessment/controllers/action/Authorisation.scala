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

package uk.gov.hmrc.mobileselfassessment.controllers.action

import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.*
import uk.gov.hmrc.api.controllers.*
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.*
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{AuthorisedFunctions, ConfidenceLevel, CredentialStrength, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mobileselfassessment.connectors.CitizenDetailsConnector
import uk.gov.hmrc.mobileselfassessment.controllers.*
import uk.gov.hmrc.mobileselfassessment.model.SaUtr
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait Authorisation extends Results with AuthorisedFunctions {

  val cdConnector: CitizenDetailsConnector
  val enableITSA: Boolean
  val confLevel: Int
  val logger: Logger = Logger(this.getClass)

  lazy val requiresAuth = true
  lazy val lowConfidenceLevel = new AccountWithLowCL
  lazy val utrNotFoundOnAccount = new UtrNotFoundOnAccount
  lazy val failedToMatchUtr = new FailToMatchTaxIdOnAuth

  def grantAccess(
    requestedUtr: SaUtr
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] =
    authorised(CredentialStrength("strong") and ConfidenceLevel.L200)
      .retrieve(nino and confidenceLevel and allEnrolments) { case foundNino ~ foundConfidenceLevel ~ enrolments =>
        val activatedUtr = getActivatedSaUtr(enrolments)
        val isMTDEnrolmentPresent = if (enableITSA) checkMtdEnrolent(enrolments) else None
        if (activatedUtr.isEmpty) {
          if (isMTDEnrolmentPresent.contains(true)) {
            cdConnector.getUtrByNino(foundNino.getOrElse("")).map {
              case Some(utr) => if (utr.utr.equals(requestedUtr.utr)) true else throw failedToMatchUtr
              case _         => false
            }
          } else throw utrNotFoundOnAccount
        } else {
          if (activatedUtr.getOrElse(SaUtr("")).utr.equals(requestedUtr.utr)) Future successful true else throw failedToMatchUtr
        }
        if (confLevel > foundConfidenceLevel.level) throw lowConfidenceLevel
        else Future successful true
      }

  def invokeAuthBlock[A](
    request: Request[A],
    block: Request[A] => Future[Result],
    saUtr: SaUtr
  )(implicit ec: ExecutionContext): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    grantAccess(saUtr)
      .flatMap { _ =>
        block(request)
      }
      .recover {
        case _: uk.gov.hmrc.http.UpstreamErrorResponse =>
          logger.info("Unauthorized! Failed to grant access since 4xx response!")
          Unauthorized(Json.toJson(ErrorUnauthorizedUpstream.asInstanceOf[ErrorResponse]))

        case _: UtrNotFoundOnAccount =>
          logger.info("Unauthorized! UTR not found on account!")
          Unauthorized(Json.toJson[ErrorResponse](ErrorUnauthorizedNoUtr))

        case _: FailToMatchTaxIdOnAuth =>
          logger.info("Forbidden! Failure to match URL UTR against Auth UTR")
          Forbidden(Json.toJson[ErrorResponse](ForbiddenAccess))

        case _: AccountWithLowCL =>
          logger.info("Unauthorized! Account with low CL!")
          Unauthorized(Json.toJson(ErrorUnauthorizedLowCL.asInstanceOf[ErrorResponse]))
      }
  }

  private def getActivatedSaUtr(enrolments: Enrolments): Option[SaUtr] =
    enrolments.enrolments
      .find(_.key == "IR-SA")
      .flatMap { enrolment =>
        enrolment.identifiers
          .find(id => id.key == "UTR" && enrolment.state == "Activated")
          .map(key => SaUtr(key.value))
      }

  private def checkMtdEnrolent(enrolments: Enrolments): Option[Boolean] =
    enrolments.enrolments
      .find(_.key == "HMRC-MTD-ID")
      .flatMap { enrolment =>
        enrolment.identifiers
          .find(id => id.key.toUpperCase == "MTDITID" && enrolment.state == "Activated")
          .map(key => true)
      }
}

trait AccessControl extends HeaderValidator with Authorisation {
  outer =>
  def parser: BodyParser[AnyContent]

  def validateAcceptWithAuth(
    rules: Option[String] => Boolean,
    saUtr: SaUtr
  )(implicit ec: ExecutionContext): ActionBuilder[Request, AnyContent] =
    new ActionBuilder[Request, AnyContent] {

      override def parser: BodyParser[AnyContent] = outer.parser
      override protected def executionContext: ExecutionContext = outer.executionContext

      def invokeBlock[A](
        request: Request[A],
        block: Request[A] => Future[Result]
      ): Future[Result] =
        if (rules(request.headers.get("Accept"))) {
          if (requiresAuth) invokeAuthBlock(request, block, saUtr)
          else block(request)
        } else
          Future.successful(
            Status(ErrorAcceptHeaderInvalid.httpStatusCode)(
              Json.toJson(ErrorAcceptHeaderInvalid.asInstanceOf[ErrorResponse])
            )
          )
    }
}
