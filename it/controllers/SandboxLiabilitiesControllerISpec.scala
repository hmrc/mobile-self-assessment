package controllers

import play.api.libs.json.Json
import play.api.libs.ws.WSRequest
import stubs.AuthStub._
import utils.BaseISpec
import uk.gov.hmrc.mobileselfassessment.model.SaUtr
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
    }
  }

//  "when request authorisation fails it" should {
//    "return 400" in {
//      authorisationRejected()
//      val body =
//        """{
//          | "accountSummary": {
//          |    "totalAmountDueToHmrc": {
//          |      "amount": "12345.67",
//          |      "requiresPayment": "true"
//          |    },
//          |    "nextPayment": {
//          |      "dueDate": "2014-01-31",
//          |      "amount": "12345.67"
//          |    },
//          |    "amountHmrcOwe": "0"
//          |    },
//          | "futureLiability": [
//          | {
//          |    "descriptionCode": "BCD",
//          |    "dueDate": "2015-01-31",
//          |    "amount": "503.2",
//          |    "taxYear": {
//          |      "start": "2014",
//          |      "end": "2015"
//          |    },
//          | {
//          |    "descriptionCode": "IN1",
//          |    "partnershipReference": "1097172564",
//          |    "dueDate": "2015-01-31",
//          |    "amount": 2300,
//          |    "taxYear": {
//          |      "start": 2014,
//          |      "end": 2015
//          |    }
//          | }
//          |]
//          |}""".stripMargin
//      val request: WSRequest = wsUrl(
//        s"/$utr/liabilities?journeyId=$journeyId"
//      ).addHttpHeaders(acceptJsonHeader, sandboxHeader)
//      val response = await(request.get())
//      response.status shouldBe 400
//    }
//  }
//
//  "when payload contains no dueDate property" should {
//    "return 400" in {
//      grantAccess()
//      val body =
//        """{
//          | "accountSummary": {
//          |    "totalAmountDueToHmrc": {
//          |      "amount": "12345.67",
//          |      "requiresPayment": "true"
//          |    },
//          |      "nextPayment": {
//          |      "dueDate": "",
//          |      "amount": "12345.67"
//          |    },
//          |    "amountHmrcOwe": "0"
//          |    },
//          | "futureLiability": [
//          | {
//          |    "descriptionCode": "BCD",
//          |    "dueDate": "2015-01-31",
//          |    "amount": "503.2",
//          |    "taxYear": {
//          |      "start": "2014",
//          |      "end": "2015"
//          |    },
//          | {
//          |    "descriptionCode": "IN1",
//          |    "partnershipReference": "1097172564",
//          |    "dueDate": "2015-01-31",
//          |    "amount": 2300,
//          |    "taxYear": {
//          |      "start": 2014,
//          |      "end": 2015
//          |    }
//          | }
//          |]
//          |}""".stripMargin
//      val request: WSRequest = wsUrl(
//        s"/$utr/liabilities?journeyId=$journeyId"
//      ).addHttpHeaders(acceptJsonHeader, sandboxHeader)
//      val response = await(request.get())
//      response.status shouldBe 400
//    }
//  }
}
