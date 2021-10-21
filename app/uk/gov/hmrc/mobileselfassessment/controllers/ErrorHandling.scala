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

import play.api.libs.json.Json.toJson
import play.api.mvc.Result
import play.api.{Logger, mvc}
import uk.gov.hmrc.api.controllers.{ErrorInternalServerError, ErrorNotFound, ErrorResponse}
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendBaseController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case object ErrorUnauthorizedUpstream
    extends ErrorResponse(401, "UNAUTHORIZED", "Upstream service such as auth returned 401")

class GrantAccessException(message: String) extends HttpException(message, 401)

class AuthIdNotFound extends GrantAccessException("Unauthorised! Account not found!")

class CredentialsNotFound extends GrantAccessException("Unauthorised! Credentials not found!")

class AccountWithLowCL extends GrantAccessException("Unauthorised! Account with low CL!")

trait ErrorHandling {
  self: BackendBaseController =>
  val app: String
  val logger: Logger = Logger(this.getClass)

  def log(message: String): Unit = logger.info(s"$app $message")

  def errorWrapper(func: => Future[mvc.Result])(implicit hc: HeaderCarrier): Future[Result] =
    func.recover {
      case _: NotFoundException =>
        log("Resource not found!")
        Status(ErrorNotFound.httpStatusCode)(toJson(ErrorNotFound.asInstanceOf[ErrorResponse]))

      case ex: Upstream4xxResponse if ex.upstreamResponseCode == 401 =>
        log("Upstream service returned 401")
        Status(ErrorUnauthorizedUpstream.httpStatusCode)(toJson(ErrorUnauthorizedUpstream.asInstanceOf[ErrorResponse]))

      case e: Exception =>
        logger.warn(s"Native Error - $app Internal server error: ${e.getMessage}", e)
        Status(ErrorInternalServerError.httpStatusCode)(toJson(ErrorInternalServerError.asInstanceOf[ErrorResponse]))
    }
}
