package controllers

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, stubFor, urlEqualTo}
import play.api.libs.json.Json
import play.api.libs.ws.WSRequest
import play.api.test.Helpers.contentAsString
import utils.BaseISpec
import stubs.AuthStub._
import stubs.ShutteringStub._
import stubs.CesaStub._
import uk.gov.hmrc.mobileselfassessment.model.{GetLiabilitiesResponse, SaUtr}

class LiabilitiesControllerISpec extends BaseISpec {

  private val utr: SaUtr = SaUtr("UTR123")

  "GET /:utr/liabilities" should {
    "return 200 and full GetLiabilities response when all data available" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinks(utr, getRootLinksResponse(utr))
      stubForGetAccountSummary(utr, accountSummaryResponse)
      stubForGetFutureLiabilities(utr, futureLiabilitiesResponse)

      val request: WSRequest = wsUrl(
        s"/$utr/liabilities?journeyId=$journeyId"
      ).addHttpHeaders(acceptJsonHeader)
      val response = await(request.get())
      response.status shouldBe 200
      val parsedResponse = Json.parse(response.body).as[GetLiabilitiesResponse]
      parsedResponse.accountSummary.totalAmountDueToHmrc.amount shouldBe 12345.67
      parsedResponse.accountSummary.amountHmrcOwe shouldBe 0
      parsedResponse.futureLiability.isEmpty shouldBe false
      parsedResponse.futureLiability.get.head.amount shouldBe 503.20
      parsedResponse.futureLiability.get.head.descriptionCode shouldBe "BCD"
      parsedResponse.futureLiability.get.head.taxYear.start shouldBe 2014
      parsedResponse.futureLiability.get.head.taxYear.end shouldBe 2015

    }

    "return 200 and full account summary in response when future liabilities unavailable" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinks(utr, getRootLinksResponse(utr))
      stubForGetAccountSummary(utr, accountSummaryResponse)
      stubForGetFutureLiabilities(utr, "[]")

      val request: WSRequest = wsUrl(
        s"/$utr/liabilities?journeyId=$journeyId"
      ).addHttpHeaders(acceptJsonHeader)
      val response = await(request.get())
      response.status shouldBe 200
      val parsedResponse = Json.parse(response.body).as[GetLiabilitiesResponse]
      parsedResponse.accountSummary.totalAmountDueToHmrc.amount shouldBe 12345.67
      parsedResponse.accountSummary.amountHmrcOwe shouldBe 0
      parsedResponse.futureLiability.isEmpty shouldBe true
    }

    "return 500 when accountSummary response from CESA is malformed" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinks(utr, getRootLinksResponse(utr))
      stubForGetAccountSummary(utr, accountSummaryMalformedResponse)

      val request: WSRequest = wsUrl(
        s"/$utr/liabilities?journeyId=$journeyId"
      ).addHttpHeaders(acceptJsonHeader)
      val response = await(request.get())
      response.status shouldBe 500
    }

    "return 500 when accountSummary response from CESA has invalid currency" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinks(utr, getRootLinksResponse(utr))
      stubForGetAccountSummary(utr, accountSummaryResponseInvalidCurrency)

      val request: WSRequest = wsUrl(
        s"/$utr/liabilities?journeyId=$journeyId"
      ).addHttpHeaders(acceptJsonHeader)
      val response = await(request.get())
      response.status shouldBe 500
    }

    "return 500 when accountSummary response from CESA has null value" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinks(utr, getRootLinksResponse(utr))
      stubForGetAccountSummary(utr, accountSummaryResponseNullValue)

      val request: WSRequest = wsUrl(
        s"/$utr/liabilities?journeyId=$journeyId"
      ).addHttpHeaders(acceptJsonHeader)
      val response = await(request.get())
      response.status shouldBe 500
    }

    "return 401 when a 401 is returned from CESA" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinksFailure(utr, 401)

      val request: WSRequest = wsUrl(
        s"/$utr/liabilities?journeyId=$journeyId"
      ).addHttpHeaders(acceptJsonHeader)
      val response = await(request.get())
      response.status shouldBe 401
    }

    "return 404 when a 404 is returned from CESA" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinksFailure(utr, 404)

      val request: WSRequest = wsUrl(
        s"/$utr/liabilities?journeyId=$journeyId"
      ).addHttpHeaders(acceptJsonHeader)
      val response = await(request.get())
      response.status shouldBe 404
    }

    "return 404 when no account summary link returned from CESA" in {
      grantAccess()
      stubForShutteringDisabled
      stubForGetRootLinks(utr, getRootLinksAccountSummaryNullResponse)

      val request: WSRequest = wsUrl(
        s"/$utr/liabilities?journeyId=$journeyId"
      ).addHttpHeaders(acceptJsonHeader)
      val response = await(request.get())
      response.status shouldBe 404
    }

    "return 406 when auth fails" in {
      authorisationRejected()

      val request: WSRequest = wsUrl(
        s"/$utr/liabilities?journeyId=$journeyId"
      ).addHttpHeaders(acceptJsonHeader)
      val response = await(request.get())
      response.status shouldBe 401
    }

    "return 500 when unknown error is returned from CESA" in {
      grantAccess()
      stubForGetRootLinksFailure(utr, 500)

      val request: WSRequest = wsUrl(
        s"/$utr/liabilities?journeyId=$journeyId"
      ).addHttpHeaders(acceptJsonHeader)
      val response = await(request.get())
      response.status shouldBe 500
    }

    "return 521 when shuttered" in {
      grantAccess()
      stubForShutteringEnabled

      val request: WSRequest = wsUrl(
        s"/$utr/liabilities?journeyId=$journeyId"
      ).addHttpHeaders(acceptJsonHeader)
      val response = await(request.get())
      response.status shouldBe 521
    }
  }

}