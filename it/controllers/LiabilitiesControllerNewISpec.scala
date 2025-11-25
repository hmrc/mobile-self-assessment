package controllers

import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import stubs.AuthStub.*
import stubs.CesaStub.*
import stubs.ShutteringStub.*
import uk.gov.hmrc.mobileselfassessment.model.{GetLiabilitiesResponse, SaUtr, Shuttering}
import uk.gov.hmrc.time.TaxYear
import utils.{BaseISpec, BaseNewISpec}

class LiabilitiesControllerNewISpec extends BaseNewISpec {

  private val utr: SaUtr = SaUtr("UTR123")

  "GET /:utr/liabilities" should {
    "return 200 and full GetLiabilities response when all data available" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetFutureLiabilitiesViaHip(utr, hipResponseJson1)

      val currentTaxYear =
        TaxYear.current.currentYear.toString.substring(2).concat(TaxYear.current.finishYear.toString.substring(2))

      val request = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 200
      val parsedResponse = Json.parse(response.body).as[GetLiabilitiesResponse]
      parsedResponse.accountSummary.totalAmountDueToHmrc.amount                               shouldBe 12345.67
      parsedResponse.accountSummary.amountHmrcOwe                                             shouldBe 0
      parsedResponse.accountSummary.taxToPayStatus.toString                                   shouldBe "OverdueWithBill"
      parsedResponse.accountSummary.nextBill.isEmpty                                          shouldBe false
      parsedResponse.accountSummary.nextBill.get.amount                                       shouldBe 2803.20
      parsedResponse.accountSummary.nextBill.get.dueDate.toString                             shouldBe "2015-01-31"
      parsedResponse.accountSummary.totalFutureLiability.get                                  shouldBe 7403.2
      parsedResponse.accountSummary.remainingAfterCreditDeducted                              shouldBe None
      parsedResponse.futureLiability.isEmpty                                                  shouldBe false
      parsedResponse.futureLiability.get.head.dueDate.toString                                shouldBe "2015-01-31"
      parsedResponse.futureLiability.get.head.total                                           shouldBe 2803.20
      parsedResponse.futureLiability.get.head.futureLiabilities.head.amount                   shouldBe 503.20
      parsedResponse.futureLiability.get.head.futureLiabilities.head.descriptionCode.toString shouldBe "JEP"
      parsedResponse.futureLiability.get.head.futureLiabilities.head.taxYear.start            shouldBe 2014
      parsedResponse.futureLiability.get.head.futureLiabilities.head.taxYear.end              shouldBe 2015
      parsedResponse.setUpPaymentPlanUrl          shouldBe "/pay-what-you-owe-in-instalments/arrangement/determine-eligibility"
      parsedResponse.updateOrSubmitAReturnUrl     shouldBe "https://www.tax.service.gov.uk/personal-account/self-assessment-summary"
      parsedResponse.viewPaymentHistoryUrl        shouldBe s"/self-assessment/ind/$utr/account/payments"
      parsedResponse.viewOtherYearsUrl            shouldBe s"/self-assessment/ind/$utr/account/taxyear/$currentTaxYear"
      parsedResponse.moreSelfAssessmentDetailsUrl shouldBe "/personal-account/self-assessment-summary"
      parsedResponse.payByDebitOrCardPaymentUrl   shouldBe "/personal-account/self-assessment-summary"
      parsedResponse.claimRefundUrl               shouldBe s"/contact/self-assessment/ind/$utr/repayment"
      parsedResponse.spreadCostUrl                shouldBe s"http://spread-the-cost"

    }

    "return 200 and full account summary in response when future liabilities unavailable" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetFutureLiabilitiesViaHip(utr, hipResponseJsonNoFutureLiability)

      val request = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 200
      val parsedResponse = Json.parse(response.body).as[GetLiabilitiesResponse]
      parsedResponse.accountSummary.totalAmountDueToHmrc.amount shouldBe 12345.67
      parsedResponse.accountSummary.amountHmrcOwe               shouldBe 0
      parsedResponse.accountSummary.taxToPayStatus.toString     shouldBe "Overdue"
      parsedResponse.accountSummary.nextBill.isEmpty            shouldBe true
      parsedResponse.futureLiability.isEmpty                    shouldBe true
    }

    "return 500 when accountSummary response from ITSA is malformed" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetFutureLiabilitiesViaHip(utr, hipResponseJsonMalformed)

      val request = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 500
    }

    "return 401 when a 404 is returned from ITSA" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetFutureLiabilitiesViaHip(utr, hip404Error, 404)

      val request = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 404
    }
  }

}
