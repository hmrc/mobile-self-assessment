package controllers

import play.api.libs.json.Json
import play.api.libs.ws.WSRequest
import stubs.AuthStub._
import utils.BaseISpec
import uk.gov.hmrc.mobileselfassessment.model.{GetLiabilitiesResponse, SaUtr}
import stubs.ShutteringStub._

class SandboxLiabilitiesControllerISpec extends BaseISpec {

  val sandboxHeader = "X-MOBILE-USER-ID" -> "208606423740"
  private val utr: SaUtr = SaUtr("UTR123")

  "when payload valid and sandbox header present it" should {
    "return 201" in {
      grantAccess()
      stubForShutteringDisabled
      val request: WSRequest = wsUrl(
        s"/$utr/liabilities?journeyId=$journeyId"
      ).addHttpHeaders(acceptJsonHeader, sandboxHeader)
      val response = await(request.get())
      response.status shouldBe 200
      val parsedResponse = Json.parse(response.body).as[GetLiabilitiesResponse]
      parsedResponse.accountSummary.totalAmountDueToHmrc.amount shouldBe 12345.67
      parsedResponse.futureLiability.get.head.futureLiabilities.head.descriptionCode.toString shouldBe "BCD"
      parsedResponse.futureLiability.get.head.futureLiabilities.head.amount shouldBe 503.2
    }
  }

  "when request authorisation fails it" should {
    "return 401" in {
      authorisationRejected()
      stubForShutteringDisabled
      val request: WSRequest = wsUrl(
        s"/$utr/liabilities?journeyId=$journeyId"
      ).addHttpHeaders(acceptJsonHeader, sandboxHeader)
      val response = await(request.get())
      response.status shouldBe 401
    }
  }

  "when service is shuttered it" should {
    "return 521" in {
      grantAccess()
      stubForShutteringEnabled
      val request: WSRequest = wsUrl(
        s"/$utr/liabilities?journeyId=$journeyId"
      ).addHttpHeaders(acceptJsonHeader, sandboxHeader)
      val response = await(request.get())
      response.status shouldBe 521
    }
  }
}
