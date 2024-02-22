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

package uk.gov.hmrc.mobileselfassessment.cesa

import org.joda.time.{DateTimeFieldType, LocalDate}
import play.api.libs.json.Json
import uk.gov.hmrc.mobileselfassessment.model.{AccountSummary, AmountDue, DescriptionCode, FutureLiability, NextBill, SaUtr, TaxYear}
import play.api.libs.json.JodaReads._
import play.api.libs.json.JodaWrites._

import scala.math.BigDecimal

object CesaRootLinks {
  implicit val formats = Json.format[CesaRootLinks]
}

case class CesaRootLinks(accountSummary: Option[String])

object CesaRootLinksWrapper {
  implicit val formats = Json.format[CesaRootLinksWrapper]
}

case class CesaRootLinksWrapper(links: CesaRootLinks)

object CesaAmount {
  implicit val formats = Json.format[CesaAmount]
}

case class CesaAmount(
  amount:   BigDecimal,
  currency: String) {

  private val paymentThreshold = BigDecimal(32d)

  if (currency != "GBP") {
    throw new CesaInvalidDataException(s"Currency string is '$currency'.  The only valid value for this is 'GBP'")
  }

  lazy val toSaAmount: BigDecimal = amount

  lazy val toSaAmountDue: AmountDue = {
    AmountDue(amount, amount > paymentThreshold)
  }
}

object CesaLiability {
  implicit val formats = Json.format[CesaLiability]
}

case class CesaLiability(
  paymentDueDate: Option[LocalDate],
  amount:         CesaAmount)

object CesaAccountSummary {
  implicit val formats = Json.format[CesaAccountSummary]
}

case class CesaAccountSummary(
  totalAmountDueToHmrc: CesaAmount,
  amountHmrcOwe:        CesaAmount) {

  lazy val toSaAccountSummary: AccountSummary = AccountSummary(
    totalAmountDueToHmrc = totalAmountDueToHmrc.toSaAmountDue,
    amountHmrcOwe        = amountHmrcOwe.toSaAmount
  )
}

object CesaFutureLiability {
  implicit val formats = Json.format[CesaFutureLiability]
}

case class CesaFutureLiability(
  statutoryDueDate:     LocalDate,
  taxYearEndDate:       LocalDate,
  partnershipReference: Option[Long],
  amount:               CesaAmount,
  descriptionCode:      DescriptionCode) {

  private def getPartnershipRefString(partnershipReference: Option[Long]): Option[SaUtr] =
    partnershipReference match {
      case None => None
      case Some(a: Long) if a == 0 => None
      case Some(value) => Some(SaUtr(value.toString))
    }

  lazy val toSaFutureLiability: FutureLiability =
    FutureLiability(
      descriptionCode,
      getPartnershipRefString(partnershipReference),
      java.time.LocalDate.of(statutoryDueDate.getYear, statutoryDueDate.getMonthOfYear, statutoryDueDate.getDayOfMonth),
      amount.toSaAmount,
      taxYear = TaxYear.fromEndYear(taxYearEndDate.get(DateTimeFieldType.year))
    )

}
