package controllers

import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import stubs.AuthStub.*
import stubs.CesaStub.*
import stubs.ShutteringStub.*
import uk.gov.hmrc.mobileselfassessment.model.{GetLiabilitiesResponse, SaUtr, Shuttering}
import uk.gov.hmrc.time.TaxYear
import utils.BaseHipCesaISpec

class LiabilitiesControllerHipCesaISpec extends BaseHipCesaISpec {

  private val utr: SaUtr = SaUtr("UTR123")

  "GET /:utr/liabilities" should {

    "return 200 and full GetLiabilities response when all data available and DWIt flag is enabled" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinks(utr, getRootLinksResponse(utr, true), true)
      stubForGetAccountSummary(utr, accountSummaryResponse, true)
      stubForGetFutureLiabilities(utr, futureLiabilitiesResponse, true)

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
      parsedResponse.accountSummary.totalFutureLiability.get                                  shouldBe 9703.20
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
      parsedResponse.spreadCostUrl                shouldBe s"/personal-account/sa/spread-the-cost-of-your-self-assessment"
      parsedResponse.selfAssessmentCessationUrl   shouldBe s"/cessation-mobile"

    }

    "return 500 when accountSummary response from CESA is malformed" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinks(utr, getRootLinksResponse(utr, true), true)
      stubForGetAccountSummary(utr, accountSummaryMalformedResponse, true)

      val request = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 500
    }

    "return 500 when accountSummary response from CESA has invalid currency" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinks(utr, getRootLinksResponse(utr, true), true)
      stubForGetAccountSummary(utr, accountSummaryResponseInvalidCurrency, true)

      val request = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 500
    }

    "return 500 when accountSummary response from CESA has null value" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinks(utr, getRootLinksResponse(utr, true), true)
      stubForGetAccountSummary(utr, accountSummaryResponseNullValue, true)

      val request = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 500
    }

    "return 401 when a 401 is returned from CESA" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinksFailure(utr, 401, true)

      val request = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 401
    }

    "return 429 when a 429 is returned from CESA" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinksFailure(utr, 429, true)

      val request = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 429
    }

    "return 404 when a 404 is returned from CESA" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinksFailure(utr, 404, true)

      val request = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 404
    }

    "return 404 when no account summary link returned from CESA" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinks(utr, getRootLinksAccountSummaryNullResponse, true)

      val request = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 404
    }

    "return 500 when unknown error is returned from CESA" in {
      grantAccess()
      stubForGetRootLinksFailure(utr, 500, true)

      val request = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 500
    }

  }

}
