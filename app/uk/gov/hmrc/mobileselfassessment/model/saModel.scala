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

import org.joda.time.LocalDate
import play.api.libs.json.Json
import play.api.libs.json.JodaReads._
import play.api.libs.json.JodaWrites._

object NextBill {
  implicit val formats = Json.format[NextBill]
}

case class NextBill(
  dueDate:       LocalDate,
  amount:        BigDecimal,
  daysRemaining: Int)

object AmountDue {
  implicit val formats = Json.format[AmountDue]
}

case class AmountDue(
  amount:          BigDecimal,
  requiresPayment: Boolean)

object AccountSummary {
  implicit val formats = Json.format[AccountSummary]
}

case class AccountSummary(
  taxToPayStatus:               TaxToPayStatus = NoTaxToPay,
  totalAmountDueToHmrc:         AmountDue,
  amountHmrcOwe:                BigDecimal,
  totalFutureLiability:         Option[BigDecimal] = None,
  nextBill:                     Option[NextBill] = None,
  remainingAfterCreditDeducted: Option[BigDecimal] = None)

object TaxYear {
  implicit val formats = Json.format[TaxYear]

  def fromEndYear(taxEndYear: Int) = TaxYear(taxEndYear - 1, taxEndYear)
}

case class TaxYear(
  start: Int,
  end:   Int)

object FutureLiability {
  implicit val formats = Json.format[FutureLiability]
}

case class FutureLiability(
  descriptionCode:      DescriptionCode,
  partnershipReference: Option[SaUtr],
  dueDate:              LocalDate,
  amount:               BigDecimal,
  taxYear:              TaxYear)

object GroupedFutureLiabilities {
  implicit val formats = Json.format[GroupedFutureLiabilities]
}

case class GroupedFutureLiabilities(
  dueDate:           LocalDate,
  futureLiabilities: Seq[FutureLiability],
  total:             BigDecimal)
