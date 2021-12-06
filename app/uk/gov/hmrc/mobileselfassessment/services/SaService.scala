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

package uk.gov.hmrc.mobileselfassessment.services

import org.joda.time.{Days, LocalDate}

import javax.inject.{Inject, Singleton}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mobileselfassessment.cesa.CesaRootLinks
import uk.gov.hmrc.mobileselfassessment.connectors.CesaIndividualsConnector
import uk.gov.hmrc.mobileselfassessment.model.{AccountSummary, AmountDue, CreditAndBillSame, CreditLessThanBill, CreditMoreThanBill, FutureLiability, GetLiabilitiesResponse, NextBill, NoTaxToPay, OnlyBill, OnlyCredit, Overdue, OverdueWithBill, SaUtr, TaxToPayStatus}
import uk.gov.hmrc.time.TaxYear

import java.time.Period
import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal

@Singleton
class SaService @Inject() (cesaConnector: CesaIndividualsConnector) extends Logging {

  def getAccountSummary(
    utr:         SaUtr
  )(implicit hc: HeaderCarrier,
    ec:          ExecutionContext
  ): Future[Option[AccountSummary]] =
    cesaConnector.getRootLinks(utr).flatMap {
      case CesaRootLinks(Some(accountSummaryUrl)) =>
        cesaConnector.getOptionalCesaAccountSummary(utr, accountSummaryUrl).map(_.map(_.toSaAccountSummary))
      case _ => Future successful None
    }

  def getFutureLiabilities(
    utr:         SaUtr
  )(implicit hc: HeaderCarrier,
    ec:          ExecutionContext
  ): Future[Option[Seq[FutureLiability]]] = {
    val liabilities: Future[Seq[FutureLiability]] =
      cesaConnector.futureLiabilities(utr).map(_.map(_.toSaFutureLiability))
    liabilities.flatMap(list => if (list.isEmpty) Future successful None else Future successful Some(list))
  }

  def getLiabilitiesResponse(
    utr:         SaUtr
  )(implicit hc: HeaderCarrier,
    ec:          ExecutionContext
  ): Future[Option[GetLiabilitiesResponse]] =
    for {
      accountSummary    <- getAccountSummary(utr)
      futureLiabilities <- getFutureLiabilities(utr)
    } yield {
      accountSummary.map(summary =>
        GetLiabilitiesResponse(
          accountSummary               = buildAccountSummary(summary, futureLiabilities),
          futureLiability              = futureLiabilities,
          viewPaymentHistoryUrl        = s"/self-assessment/ind/$utr/account/payments",
          viewOtherYearsUrl            = s"/self-assessment/ind/$utr/account/taxyear/$currentTaxYear",
          moreSelfAssessmentDetailsUrl = s"/self-assessment/ind/$utr/account",
          claimRefundUrl               = s"/contact/self-assessment/ind/$utr/repayment",
          viewBreakdownUrl             = s"/self-assessment/ind/$utr/account"
        )
      )
    }

  private def buildAccountSummary(
    summary:     AccountSummary,
    liabilities: Option[Seq[FutureLiability]]
  ): AccountSummary = {
    val nextBill: Option[NextBill] = liabilities.flatMap(calculateNextBill)
    summary.copy(
      taxToPayStatus =
        getTaxToPayStatus(summary.totalAmountDueToHmrc.amount, summary.amountHmrcOwe, nextBill.map(_.amount)),
      nextBill                     = nextBill,
      totalFutureLiability         = liabilities.flatMap(calculateTotalLiability),
      remainingAfterCreditDeducted = calculateRemainingAfterCredit(nextBill, summary.amountHmrcOwe)
    )
  }

  private def currentTaxYear: String =
    TaxYear.current.currentYear.toString.substring(2).concat(TaxYear.current.finishYear.toString.substring(2))

  private def calculateNextBill(futureLiabilities: Seq[FutureLiability]): Option[NextBill] = {
    val nextBillDate = getNextBillDate(futureLiabilities)
    nextBillDate.map { date =>
      val daysRemaining = Days.daysBetween(LocalDate.now(), date).getDays
      NextBill(date,
               sumOfLiabilitiesOccuringOnDate(futureLiabilities, date),
               daysRemaining = if (daysRemaining < 0) -1 else daysRemaining)
    }
  }

  private def getNextBillDate(futureLiabilities: Seq[FutureLiability]): Option[LocalDate] =
    futureLiabilities.sortBy(_.dueDate.toString()).headOption.map(_.dueDate)

  private def sumOfLiabilitiesOccuringOnDate(
    liabilities:   Seq[FutureLiability],
    liabilityDate: LocalDate
  ): BigDecimal =
    liabilities.filter(_.dueDate equals liabilityDate).map(_.amount).sum

  private def getTaxToPayStatus(
    amountDue:  BigDecimal,
    amountOwed: BigDecimal,
    nextBill:   Option[BigDecimal]
  ): TaxToPayStatus =
    (amountDue, amountOwed, nextBill) match {
      case (amountDue, _, Some(nextBill)) if amountDue > 0 && nextBill > 0             => OverdueWithBill
      case (amountDue, _, _) if amountDue > 0                                          => Overdue
      case (_, amountOwed, Some(nextBill)) if amountOwed > 0 && amountOwed == nextBill => CreditAndBillSame
      case (_, amountOwed, Some(nextBill)) if amountOwed > 0 && amountOwed < nextBill  => CreditLessThanBill
      case (_, amountOwed, Some(nextBill)) if amountOwed > 0 && amountOwed > nextBill  => CreditMoreThanBill
      case (_, amountOwed, None) if amountOwed > 0                                     => OnlyCredit
      case (_, _, Some(nextBill)) if nextBill > 0                                      => OnlyBill
      case _                                                                           => NoTaxToPay
    }

  private def calculateTotalLiability(futureLiabilities: Seq[FutureLiability]): Option[BigDecimal] = {
    val total = futureLiabilities.map(_.amount).sum
    if (total > 0) Some(total) else None
  }

  private def calculateRemainingAfterCredit(
    nextBill: Option[NextBill],
    credit:   BigDecimal
  ): Option[BigDecimal] = {
    val nextBillAmount: BigDecimal = nextBill.map(_.amount).getOrElse(0)
    if (credit > 0 && nextBill.isDefined && credit != nextBillAmount) {
      if (credit > nextBillAmount)
        Some(credit - nextBillAmount)
      else
        Some(nextBillAmount - credit)
    } else None
  }
}
