package controllers

import play.api.libs.json.Json
import play.api.libs.ws.WSRequest
import utils.BaseISpec
import uk.gov.hmrc.mobileselfassessment.model.{GetLiabilitiesResponse, SaUtr}
import stubs.ShutteringStub._

class SandboxLiabilitiesControllerISpec extends BaseISpec {

  val sandboxHeader: (String, String) = "X-MOBILE-USER-ID" -> "208606423740"
  private val utr: SaUtr = SaUtr("UTR123")

  "when payload valid and sandbox header present it" should {
    "return 201" in {
      stubForShutteringDisabled
      val request: WSRequest = wsUrl(
        s"/$utr/liabilities?journeyId=$journeyId"
      ).addHttpHeaders(acceptJsonHeader, sandboxHeader)
      val response = await(request.get())
      response.status shouldBe 200
      val parsedResponse = Json.parse(response.body).as[GetLiabilitiesResponse]
      parsedResponse.accountSummary.totalAmountDueToHmrc.amount shouldBe 0
      parsedResponse.futureLiability.get.head.futureLiabilities.head.descriptionCode.toString shouldBe "IN1"
      parsedResponse.futureLiability.get.head.futureLiabilities.head.amount shouldBe 850
    }
  }

  "when request authorisation fails it" should {
    "return 406" in {
      stubForShutteringDisabled
      val request: WSRequest = wsUrl(
        s"/$utr/liabilities?journeyId=$journeyId"
      ).addHttpHeaders(sandboxHeader)
      val response = await(request.get())
      response.status shouldBe 406
    }
  }

  "when service is shuttered it" should {
    "return 521" in {
      stubForShutteringEnabled
      val request: WSRequest = wsUrl(
        s"/$utr/liabilities?journeyId=$journeyId"
      ).addHttpHeaders(acceptJsonHeader, sandboxHeader)
      val response = await(request.get())
      response.status shouldBe 521
    }
  }
}
