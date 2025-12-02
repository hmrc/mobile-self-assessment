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

package uk.gov.hmrc.mobileselfassessment.services
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.mobileselfassessment.MobileSelfAssessmentTestData
import uk.gov.hmrc.mobileselfassessment.connectors.HipConnector
import uk.gov.hmrc.mobileselfassessment.model.{BCD, ChargeDetails, GetLiabilitiesResponse, HipResponse, IN1, IN2, SaUtr}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class SaHipServiceSpec
    extends AnyWordSpec
    with MockitoSugar
    with MobileSelfAssessmentTestData
    with Matchers
    with FutureAwaits
    with DefaultAwaitTimeout {

  implicit lazy val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  private val mockHipConnector: HipConnector = mock[HipConnector]
  private val service = new SaHipService(mockHipConnector)

  private val utr = SaUtr("123UTR")

  def mockSelfAssessmentLiabilitiesData(response: Future[HipResponse]) = {
    when(
      mockHipConnector
        .getSelfAssessmentLiabilitiesData(any())(any(), any())
    )
      .thenReturn(response)
  }

  def getTaxYear: Int = {
    val now = java.time.LocalDate.now()
    val year = now.getYear % 100
    val offset = if (now.isBefore(java.time.LocalDate.of(now.getYear, 4, 6))) -1 else 0
    (year + offset) * 100 + (year + offset + 1)
  }

  "getLiabilitiesResponse" should {
    "return a full liabilities response" in {
      mockSelfAssessmentLiabilitiesData(Future.successful(hipResponse2))
      val result: GetLiabilitiesResponse = await(service.getLiabilitiesResponse(utr)).get
      result.accountSummary.totalAmountDueToHmrc.amount shouldBe 12345.67
      result.futureLiability.isEmpty                    shouldBe false
      result.setUpPaymentPlanUrl                        shouldBe "/pay-what-you-owe-in-instalments/arrangement/determine-eligibility"
      result.updateOrSubmitAReturnUrl                   shouldBe "https://www.tax.service.gov.uk/personal-account/self-assessment-summary"
      result.viewPaymentHistoryUrl                      shouldBe "/self-assessment/ind/123UTR/account/payments"
      result.viewOtherYearsUrl                          shouldBe s"/self-assessment/ind/123UTR/account/taxyear/$getTaxYear"
      result.moreSelfAssessmentDetailsUrl               shouldBe "/personal-account/self-assessment-summary"
      result.payByDebitOrCardPaymentUrl                 shouldBe "/personal-account/self-assessment-summary"
      result.claimRefundUrl                             shouldBe "/contact/self-assessment/ind/123UTR/repayment"
      result.spreadCostUrl                              shouldBe "/personal-account/sa/spread-the-cost-of-your-self-assessment"

    }

    "Throw NotFoundException if returned from ITSA" in {
      mockSelfAssessmentLiabilitiesData(Future failed new NotFoundException("Account not found"))
      intercept[NotFoundException] {
        await(service.getLiabilitiesResponse(utr))
      }
    }

    "return accountSummary with no futureLiabilities if no futureLiabilities are returned from ITSA" in {

      mockSelfAssessmentLiabilitiesData(Future.successful(hipResponse2.copy(chargeDetails = List.empty)))
      val result: GetLiabilitiesResponse = await(service.getLiabilitiesResponse(utr)).get
      result.accountSummary.totalAmountDueToHmrc.amount shouldBe 12345.67
      result.futureLiability.isEmpty                    shouldBe true
    }

    "return TaxToPayStatus as Overdue and nextBill correctly" in {
      mockSelfAssessmentLiabilitiesData(Future.successful(hipResponse2.copy(chargeDetails = List.empty)))
      val result: GetLiabilitiesResponse = await(service.getLiabilitiesResponse(utr)).get
      result.accountSummary.taxToPayStatus.toString shouldBe "Overdue"
      result.accountSummary.nextBill.isEmpty        shouldBe true
    }

    "return TaxToPayStatus as CreditLessThanBill status with nextBill and remainingAfterCreditDeducted calculated correctly" in {
      mockSelfAssessmentLiabilitiesData(Future.successful(hipResponse3(650.20, 0, 500)))
      val result: GetLiabilitiesResponse = await(service.getLiabilitiesResponse(utr)).get
      result.accountSummary.taxToPayStatus.toString       shouldBe "CreditLessThanBill"
      result.accountSummary.nextBill.get.amount           shouldBe 850.20
      result.accountSummary.nextBill.get.dueDate.toString shouldBe "2020-01-31"
      result.accountSummary.nextBill.get.daysRemaining    shouldBe -1
      result.accountSummary.remainingAfterCreditDeducted  shouldBe Some(350.20)
      result.accountSummary.totalFutureLiability          shouldBe Some(6050.20)

    }

    "return TaxToPayStatus as CreditMoreThanBill status with nextBill and remainingAfterCreditDeducted calculated correctly" in {
      mockSelfAssessmentLiabilitiesData(Future.successful(hipResponse3(259.50, 0, 1000)))
      val result: GetLiabilitiesResponse = await(service.getLiabilitiesResponse(utr)).get
      result.accountSummary.taxToPayStatus.toString       shouldBe "CreditMoreThanBill"
      result.accountSummary.nextBill.get.amount           shouldBe 459.50
      result.accountSummary.nextBill.get.dueDate.toString shouldBe "2020-01-31"
      result.accountSummary.nextBill.get.daysRemaining    shouldBe -1
      result.accountSummary.remainingAfterCreditDeducted  shouldBe Some(540.50)
      result.accountSummary.totalFutureLiability          shouldBe Some(5659.50)
    }

    "return TaxToPayStatus as OnlyCredit and nextBill correctly" in {
      mockSelfAssessmentLiabilitiesData(Future.successful(hipResponse3(1, 0, 500).copy(chargeDetails = List.empty)))
      val result: GetLiabilitiesResponse = await(service.getLiabilitiesResponse(utr)).get
      result.accountSummary.taxToPayStatus.toString shouldBe "OnlyCredit"
      result.accountSummary.nextBill.isEmpty        shouldBe true
    }

    "return TaxToPayStatus as NoTaxToPay and nextBill correctly" in {
      mockSelfAssessmentLiabilitiesData(Future.successful(hipResponse3(0, 0, 0).copy(chargeDetails = List.empty)))
      val result: GetLiabilitiesResponse = await(service.getLiabilitiesResponse(utr)).get
      result.accountSummary.taxToPayStatus.toString shouldBe "NoTaxToPay"
      result.accountSummary.nextBill.isEmpty        shouldBe true
    }

    "return TaxToPayStatus as OverdueWithBill and nextBill correctly" in {

      mockSelfAssessmentLiabilitiesData(Future.successful(hipResponse2))
      val result: GetLiabilitiesResponse = await(service.getLiabilitiesResponse(utr)).get
      result.accountSummary.taxToPayStatus.toString       shouldBe "OverdueWithBill"
      result.accountSummary.nextBill.get.amount           shouldBe 2803.20
      result.accountSummary.nextBill.get.dueDate.toString shouldBe "2015-01-31"
      result.accountSummary.nextBill.get.daysRemaining    shouldBe -1
      result.accountSummary.totalFutureLiability          shouldBe Some(7403.20)
    }

    "calculate daysRemaining till next bill correctly" in {
      mockSelfAssessmentLiabilitiesData(
        Future.successful(
          hipResponse3(0, 0, 0).copy(chargeDetails =
            List(
              ChargeDetails("IN1", 200, "2020", LocalDate.now().plusDays(31))
            )
          )
        )
      )

      val result: GetLiabilitiesResponse = await(service.getLiabilitiesResponse(utr)).get
      result.accountSummary.taxToPayStatus.toString       shouldBe "OnlyBill"
      result.accountSummary.nextBill.get.amount           shouldBe 200
      result.accountSummary.nextBill.get.dueDate.toString shouldBe LocalDate.now().plusDays(31).toString()
      result.accountSummary.nextBill.get.daysRemaining    shouldBe 31
    }

    "group liabilities by date and provide total" in {
      mockSelfAssessmentLiabilitiesData(Future.successful(hipResponse2))
      val result: GetLiabilitiesResponse = await(service.getLiabilitiesResponse(utr)).get
      result.futureLiability.isEmpty                                                  shouldBe false
      result.futureLiability.get.size                                                 shouldBe 3
      result.futureLiability.get.head.dueDate.toString                                shouldBe "2015-01-31"
      result.futureLiability.get.head.total                                           shouldBe 2803.20
      result.futureLiability.get.head.futureLiabilities.size                          shouldBe 2
      result.futureLiability.get.head.futureLiabilities.head.amount                   shouldBe 503.20
      result.futureLiability.get.head.futureLiabilities.head.descriptionCode.toString shouldBe "JEP"
      result.futureLiability.get.head.futureLiabilities.head.taxYear.start            shouldBe 2014
      result.futureLiability.get.head.futureLiabilities.head.taxYear.end              shouldBe 2015
      result.futureLiability.get.last.dueDate.toString                                shouldBe "2016-06-28"
      result.futureLiability.get.last.total                                           shouldBe 2300
      result.futureLiability.get.last.futureLiabilities.size                          shouldBe 1
      result.futureLiability.get.last.futureLiabilities.head.amount                   shouldBe 2300
      result.futureLiability.get.last.futureLiabilities.head.descriptionCode.toString shouldBe "PP2"
      result.futureLiability.get.last.futureLiabilities.head.taxYear.start            shouldBe 2014
      result.futureLiability.get.last.futureLiabilities.head.taxYear.end              shouldBe 2015
    }
  }

}
