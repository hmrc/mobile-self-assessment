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

import java.time.LocalDate

case class ChargeDetails(
  chargeType: String,
  outstandingAmount: BigDecimal,
  taxYear: String,
  dueDate: LocalDate
) {
  require(outstandingAmount >= 0)
}

object ChargeDetails {
  implicit val format: OFormat[ChargeDetails] = Json.format[ChargeDetails]

  def toFutureLiabilities(chargeDetails: List[ChargeDetails]) = {
    chargeDetails.map { chargeDetail =>
      FutureLiability(
        descriptionCode      = DescriptionCode.fromString(chargeDetail.chargeType),
        partnershipReference = None,
        dueDate = java.time.LocalDate.of(chargeDetail.dueDate.getYear, chargeDetail.dueDate.getMonthValue, chargeDetail.dueDate.getDayOfMonth),
        amount  = chargeDetail.outstandingAmount,
        taxYear = TaxYear(chargeDetail.taxYear.toInt, chargeDetail.taxYear.toInt + 1)
      )
    }
  }
}
