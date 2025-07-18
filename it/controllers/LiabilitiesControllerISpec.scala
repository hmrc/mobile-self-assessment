package controllers

import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import utils.BaseISpec
import stubs.AuthStub.*
import stubs.ShutteringStub.*
import stubs.CesaStub.*
import uk.gov.hmrc.mobileselfassessment.model.{GetLiabilitiesResponse, SaUtr, Shuttering}
import uk.gov.hmrc.time.TaxYear

class LiabilitiesControllerISpec extends BaseISpec {

  private val utr: SaUtr = SaUtr("UTR123")

  "GET /:utr/liabilities" should {
    "return 200 and full GetLiabilities response when all data available" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinks(utr, getRootLinksResponse(utr))
      stubForGetAccountSummary(utr, accountSummaryResponse)
      stubForGetFutureLiabilities(utr, futureLiabilitiesResponse)

      val currentTaxYear =
        TaxYear.current.currentYear.toString.substring(2).concat(TaxYear.current.finishYear.toString.substring(2))

      val request  = s"/$utr/liabilities?journeyId=$journeyId"
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
      parsedResponse.setUpPaymentPlanUrl                                                      shouldBe "/pay-what-you-owe-in-instalments/arrangement/determine-eligibility"
      parsedResponse.updateOrSubmitAReturnUrl                                                 shouldBe "https://www.tax.service.gov.uk/personal-account/self-assessment-summary"
      parsedResponse.viewPaymentHistoryUrl                                                    shouldBe s"/self-assessment/ind/$utr/account/payments"
      parsedResponse.viewOtherYearsUrl                                                        shouldBe s"/self-assessment/ind/$utr/account/taxyear/$currentTaxYear"
      parsedResponse.moreSelfAssessmentDetailsUrl                                             shouldBe "/personal-account/self-assessment-summary"
      parsedResponse.payByDebitOrCardPaymentUrl                                               shouldBe "/personal-account/self-assessment-summary"
      parsedResponse.claimRefundUrl                                                           shouldBe s"/contact/self-assessment/ind/$utr/repayment"

    }

    "return 200 and full account summary in response when future liabilities unavailable" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinks(utr, getRootLinksResponse(utr))
      stubForGetAccountSummary(utr, accountSummaryResponse)
      stubForGetFutureLiabilities(utr, "[]")

      val request  = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 200
      val parsedResponse = Json.parse(response.body).as[GetLiabilitiesResponse]
      parsedResponse.accountSummary.totalAmountDueToHmrc.amount shouldBe 12345.67
      parsedResponse.accountSummary.amountHmrcOwe               shouldBe 0
      parsedResponse.accountSummary.taxToPayStatus.toString     shouldBe "Overdue"
      parsedResponse.accountSummary.nextBill.isEmpty            shouldBe true
      parsedResponse.futureLiability.isEmpty                    shouldBe true
    }

    "return 500 when accountSummary response from CESA is malformed" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinks(utr, getRootLinksResponse(utr))
      stubForGetAccountSummary(utr, accountSummaryMalformedResponse)

      val request  = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 500
    }

    "return 500 when accountSummary response from CESA has invalid currency" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinks(utr, getRootLinksResponse(utr))
      stubForGetAccountSummary(utr, accountSummaryResponseInvalidCurrency)

      val request  = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 500
    }

    "return 500 when accountSummary response from CESA has null value" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinks(utr, getRootLinksResponse(utr))
      stubForGetAccountSummary(utr, accountSummaryResponseNullValue)

      val request  = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 500
    }

    "return 401 when a 401 is returned from CESA" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinksFailure(utr, 401)

      val request  = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 401
    }

    "return 429 when a 429 is returned from CESA" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinksFailure(utr, 429)

      val request  = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 429
    }

    "return 403 for valid utr for authorised user but for a different utr" in {
      grantAccess(saUtr = "differentUtr")
      stubForShutteringDisabled

      val request  = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 403
    }

    "return 401 when no active UTR is found on account" in {
      grantAccess(activeUtr = false)
      stubForShutteringDisabled

      val request  = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 401
    }

    "return 404 when a 404 is returned from CESA" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinksFailure(utr, 404)

      val request  = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 404
    }

    "return 404 when no account summary link returned from CESA" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinks(utr, getRootLinksAccountSummaryNullResponse)

      val request  = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 404
    }

    "return 406 when auth fails" in {
      authorisationRejected()

      val request  = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 401
    }

    "return 400 when journeyId is not supplied" in {
      val request  = s"/$utr/liabilities"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 400
    }

    "return 406 when acceptHeader is missing" in {
      val request:  WSRequest  = wsUrl(s"/$utr/liabilities?journeyId=$journeyId")
      val response: WSResponse = await(request.get())
      response.status shouldBe 406
    }

    "return 500 when unknown error is returned from CESA" in {
      grantAccess()
      stubForGetRootLinksFailure(utr, 500)

      val request  = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 500
    }

    "return 521 when shuttered" in {
      grantAccess()
      stubForShutteringEnabled

      val request  = s"/$utr/liabilities?journeyId=$journeyId"
      val response = await(getRequestWithAuthHeaders(request))
      response.status shouldBe 521
    }
  }

}
