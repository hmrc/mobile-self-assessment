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

package uk.gov.hmrc.mobileselfassessment.model

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.mobileselfassessment.model.ChargeDetails

case class HipResponse(
  balanceDetails: BalanceDetails,
  chargeDetails: List[ChargeDetails]
) {

  private val paymentThreshold = BigDecimal(32d)

  lazy val toSaAmountDue: AmountDue = {
    val amount = balanceDetails.totalBalance
    AmountDue(amount, amount > paymentThreshold)
  }

  lazy val toSaAccountSummary: Option[AccountSummary] = {
    if (balanceDetails.totalBalance > 0 || balanceDetails.totalCreditAvailable > 0)
      Some(
        AccountSummary(
          totalAmountDueToHmrc = toSaAmountDue,
          amountHmrcOwe        = balanceDetails.totalCreditAvailable
        )
      )
    else None
  }
}

object HipResponse {
  implicit val format: OFormat[HipResponse] = Json.format[HipResponse]
}
