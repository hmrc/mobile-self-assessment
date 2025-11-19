/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.mobileselfassessment.hip

import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.http.HttpException

case class HipExceptions(override val message: String) extends HttpException(message, INTERNAL_SERVER_ERROR)

case class HipError(`type`: String, reason: String)

object HipError {
  implicit val format: Format[HipError] = Json.format[HipError]
}

case class HipErrorDetails(failures: List[HipError])

object HipErrorDetails {
  implicit val format: OFormat[HipErrorDetails] = Json.format[HipErrorDetails]
}

case class HipResponseError(
  origin: String,
  service: Option[String],
  response: HipErrorDetails
)

object HipResponseError {
  implicit val format: OFormat[HipResponseError] = Json.format[HipResponseError]
}
