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

package uk.gov.hmrc.mobileselfassessment.model

import play.api.libs.json.{Json, OFormat}

case class GetLiabilitiesResponse(accountSummary: AccountSummary,
                                  futureLiability: Option[Seq[GroupedFutureLiabilities]],
                                  setUpPaymentPlanUrl: String = "/pay-what-you-owe-in-instalments/arrangement/determine-eligibility",
                                  updateOrSubmitAReturnUrl: String = "https://www.tax.service.gov.uk/personal-account/self-assessment-summary",
                                  viewPaymentHistoryUrl: String,
                                  viewOtherYearsUrl: String,
                                  moreSelfAssessmentDetailsUrl: String = "/personal-account/self-assessment-summary",
                                  payByDebitOrCardPaymentUrl: String = "/personal-account/self-assessment-summary",
                                  claimRefundUrl: String
                                 )
object GetLiabilitiesResponse {

  implicit val format: OFormat[GetLiabilitiesResponse] = Json.format[GetLiabilitiesResponse]
}
