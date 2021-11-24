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

import org.joda.time.LocalDate
import uk.gov.hmrc.mobileselfassessment.mocks.CesaMock
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.mobileselfassessment.MobileSelfAssessmentTestData
import uk.gov.hmrc.mobileselfassessment.cesa.{CesaAccountSummary, CesaAmount, CesaFutureLiability, CesaLiability, CesaRootLinks}
import uk.gov.hmrc.mobileselfassessment.connectors.CesaIndividualsConnector
import uk.gov.hmrc.mobileselfassessment.model.{BCD, GetLiabilitiesResponse, IN1, IN2, SaUtr}

import scala.concurrent.{ExecutionContext, Future}

class SaServiceSpec
    extends AnyWordSpec
    with Matchers
    with MockFactory
    with CesaMock
    with MobileSelfAssessmentTestData
    with FutureAwaits
    with DefaultAwaitTimeout {

  implicit val mockCesaConnector: CesaIndividualsConnector = mock[CesaIndividualsConnector]
  private val service = new SaService(mockCesaConnector)
  implicit lazy val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit lazy val hc: HeaderCarrier    = HeaderCarrier()

  private val utr               = SaUtr("123UTR")
  private val rootLinks         = CesaRootLinks(Some(s"/self-assessment/individual/$utr/account-summary"))
  private val accountSummary    = Json.parse(accountSummaryResponse).as[CesaAccountSummary]
  private val futureLiabilities = Json.parse(futureLiabilitiesResponse).as[Seq[CesaFutureLiability]]

  private def customAccountSummary(
    amountDue:  BigDecimal,
    amountOwed: BigDecimal
  ): CesaAccountSummary =
    CesaAccountSummary(totalAmountDueToHmrc = CesaAmount(amountDue, "GBP"),
                       nextPayment          = CesaLiability(None, CesaAmount(0, "GBP")),
                       amountHmrcOwe        = CesaAmount(amountOwed, "GBP"))

  private def customFutureLiabilities(liabilityAmount: BigDecimal): Seq[CesaFutureLiability] =
    Seq(
      CesaFutureLiability(
        statutoryDueDate     = LocalDate.parse("2020-01-31"),
        taxYearEndDate       = LocalDate.parse("2020-03-31"),
        partnershipReference = None,
        amount               = CesaAmount(200, "GBP"),
        descriptionCode      = IN1
      ),
      CesaFutureLiability(
        statutoryDueDate     = LocalDate.parse("2020-02-28"),
        taxYearEndDate       = LocalDate.parse("2020-03-31"),
        partnershipReference = None,
        amount               = CesaAmount(5200, "GBP"),
        descriptionCode      = IN2
      ),
      CesaFutureLiability(
        statutoryDueDate     = LocalDate.parse("2020-01-31"),
        taxYearEndDate       = LocalDate.parse("2020-03-31"),
        partnershipReference = None,
        amount               = CesaAmount(liabilityAmount, "GBP"),
        descriptionCode      = BCD
      )
    )

  "getLiabilitiesResponse" should {
    "return a full liabilities response" in {
      mockGetRootLinks(Future successful rootLinks)
      mockGetOptionalCesaAccountSummary(Some(accountSummary))
      mockGetFutureLiabilities(futureLiabilities)
      val result: GetLiabilitiesResponse = await(service.getLiabilitiesResponse(utr)).get
      result.accountSummary.totalAmountDueToHmrc.amount shouldBe 12345.67
      result.futureLiability.isEmpty                    shouldBe false
      println(Json.prettyPrint(Json.toJson(result)))
    }

    "return None if no accountURL is returned in root links" in {
      mockGetRootLinks(Future successful CesaRootLinks(None))
      mockGetFutureLiabilities(futureLiabilities)
      val result = await(service.getLiabilitiesResponse(utr))
      result shouldBe None
    }

    "Throw NotFoundException if returned from Cesa" in {
      mockGetRootLinks(Future failed new NotFoundException("Account not found"))
      intercept[NotFoundException] {
        await(service.getLiabilitiesResponse(utr))
      }
    }

    "return None if no accountSummary is returned from Cesa" in {
      mockGetRootLinks(Future successful rootLinks)
      mockGetOptionalCesaAccountSummary(None)
      mockGetFutureLiabilities(futureLiabilities)
      val result = await(service.getLiabilitiesResponse(utr))
      result shouldBe None
    }

    "return accountSummary with no futureLiabilities if no futureLiabilities are returned from Cesa" in {
      mockGetRootLinks(Future successful rootLinks)
      mockGetOptionalCesaAccountSummary(Some(accountSummary))
      mockGetFutureLiabilities(Seq.empty)
      val result: GetLiabilitiesResponse = await(service.getLiabilitiesResponse(utr)).get
      result.accountSummary.totalAmountDueToHmrc.amount shouldBe 12345.67
      result.futureLiability.isEmpty                    shouldBe true
    }

    "return TaxToPayStatus as Overdue and nextBill correctly" in {
      mockGetRootLinks(Future successful rootLinks)
      mockGetOptionalCesaAccountSummary(Some(customAccountSummary(1000, 0)))
      mockGetFutureLiabilities(Seq.empty)
      val result: GetLiabilitiesResponse = await(service.getLiabilitiesResponse(utr)).get
      result.accountSummary.taxToPayStatus.toString       shouldBe "Overdue"
      result.accountSummary.nextBill.isEmpty              shouldBe true
    }

    "return TaxToPayStatus as CreditAndBillSame and nextBill correctly" in {
      mockGetRootLinks(Future successful rootLinks)
      mockGetOptionalCesaAccountSummary(Some(customAccountSummary(0, 1000)))
      mockGetFutureLiabilities(customFutureLiabilities(800))
      val result: GetLiabilitiesResponse = await(service.getLiabilitiesResponse(utr)).get
      result.accountSummary.taxToPayStatus.toString       shouldBe "CreditAndBillSame"
      result.accountSummary.nextBill.get.amount           shouldBe 1000
      result.accountSummary.nextBill.get.dueDate.toString shouldBe "2020-01-31"
    }

    "return TaxToPayStatus as CreditLessThanBill and nextBill correctly" in {
      mockGetRootLinks(Future successful rootLinks)
      mockGetOptionalCesaAccountSummary(Some(customAccountSummary(0, 500)))
      mockGetFutureLiabilities(customFutureLiabilities(800))
      val result: GetLiabilitiesResponse = await(service.getLiabilitiesResponse(utr)).get
      result.accountSummary.taxToPayStatus.toString       shouldBe "CreditLessThanBill"
      result.accountSummary.nextBill.get.amount           shouldBe 1000
      result.accountSummary.nextBill.get.dueDate.toString shouldBe "2020-01-31"
    }

    "return TaxToPayStatus as CreditMoreThanBill and nextBill correctly" in {
      mockGetRootLinks(Future successful rootLinks)
      mockGetOptionalCesaAccountSummary(Some(customAccountSummary(0, 1000)))
      mockGetFutureLiabilities(customFutureLiabilities(300))
      val result: GetLiabilitiesResponse = await(service.getLiabilitiesResponse(utr)).get
      result.accountSummary.taxToPayStatus.toString       shouldBe "CreditMoreThanBill"
      result.accountSummary.nextBill.get.amount           shouldBe 500
      result.accountSummary.nextBill.get.dueDate.toString shouldBe "2020-01-31"
    }

    "return TaxToPayStatus as OnlyCredit and nextBill correctly" in {
      mockGetRootLinks(Future successful rootLinks)
      mockGetOptionalCesaAccountSummary(Some(customAccountSummary(0, 500)))
      mockGetFutureLiabilities(Seq.empty)
      val result: GetLiabilitiesResponse = await(service.getLiabilitiesResponse(utr)).get
      result.accountSummary.taxToPayStatus.toString shouldBe "OnlyCredit"
      result.accountSummary.nextBill.isEmpty        shouldBe true
    }

    "return TaxToPayStatus as OnlyBill and nextBill correctly" in {
      mockGetRootLinks(Future successful rootLinks)
      mockGetOptionalCesaAccountSummary(Some(customAccountSummary(0, 0)))
      mockGetFutureLiabilities(customFutureLiabilities(300))
      val result: GetLiabilitiesResponse = await(service.getLiabilitiesResponse(utr)).get
      result.accountSummary.taxToPayStatus.toString       shouldBe "OnlyBill"
      result.accountSummary.nextBill.get.amount           shouldBe 500
      result.accountSummary.nextBill.get.dueDate.toString shouldBe "2020-01-31"
    }

    "return TaxToPayStatus as NoTaxToPay and nextBill correctly" in {
      mockGetRootLinks(Future successful rootLinks)
      mockGetOptionalCesaAccountSummary(Some(customAccountSummary(0, 0)))
      mockGetFutureLiabilities(Seq.empty)
      val result: GetLiabilitiesResponse = await(service.getLiabilitiesResponse(utr)).get
      result.accountSummary.taxToPayStatus.toString shouldBe "NoTaxToPay"
      result.accountSummary.nextBill.isEmpty        shouldBe true
    }

    "return TaxToPayStatus as OverdueWithBill and nextBill correctly" in {
      mockGetRootLinks(Future successful rootLinks)
      mockGetOptionalCesaAccountSummary(Some(customAccountSummary(1000, 0)))
      mockGetFutureLiabilities(futureLiabilities)
      val result: GetLiabilitiesResponse = await(service.getLiabilitiesResponse(utr)).get
      result.accountSummary.taxToPayStatus.toString       shouldBe "OverdueWithBill"
      result.accountSummary.nextBill.get.amount           shouldBe 2803.20
      result.accountSummary.nextBill.get.dueDate.toString shouldBe "2015-01-31"
    }
  }
}
