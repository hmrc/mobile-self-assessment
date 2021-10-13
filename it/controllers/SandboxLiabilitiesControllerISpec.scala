package controllers

import play.api.libs.json.Json
import play.api.libs.ws.WSRequest
import stubs.AuthStub._
import utils.BaseISpec

class SandboxLiabilitiesControllerISpec extends BaseISpec {

  val sandboxHeader = "X-MOBILE-USER-ID" -> "167927702220"

  "when payload valid and sandbox header present it" should {
    "return 201" in {
      grantAccess()
      val body =
        """{
          | "accountSummary": {
          |    "totalAmountDueToHmrc": {
          |      "amount": "12345.67",
          |      "requiresPayment": "true"
          |    },
          |    "nextPayment": {
          |      "dueDate": "2014-01-31",
          |      "amount": "12345.67"
          |    },
          |    "amountHmrcOwe": "0"
          |    },
          | "futureLiability": [
          | {
          |    "descriptionCode": "BCD",
          |    "dueDate": "2015-01-31",
          |    "amount": "503.2",
          |    "taxYear": {
          |      "start": "2014",
          |      "end": "2015"
          |    },
          | {
          |    "descriptionCode": "IN1",
          |    "partnershipReference": "1097172564",
          |    "dueDate": "2015-01-31",
          |    "amount": 2300,
          |    "taxYear": {
          |      "start": 2014,
          |      "end": 2015
          |    }
          | }
          |]
          |}""".stripMargin
      val request: WSRequest = wsUrl(
        s"/register/secure-messages?journeyId=$journeyId"
      ).addHttpHeaders(acceptJsonHeader, sandboxHeader)
      val response = await(request.get(Json.parse(body)))
      response.status shouldBe 201
    }
  }

  "when request authorisation fails it" should {
    "return 400" in {
      authorisationRejected()
      val body =
        """{
          | "accountSummary": {
          |    "totalAmountDueToHmrc": {
          |      "amount": "12345.67",
          |      "requiresPayment": "true"
          |    },
          |    "nextPayment": {
          |      "dueDate": "2014-01-31",
          |      "amount": "12345.67"
          |    },
          |    "amountHmrcOwe": "0"
          |    },
          | "futureLiability": [
          | {
          |    "descriptionCode": "BCD",
          |    "dueDate": "2015-01-31",
          |    "amount": "503.2",
          |    "taxYear": {
          |      "start": "2014",
          |      "end": "2015"
          |    },
          | {
          |    "descriptionCode": "IN1",
          |    "partnershipReference": "1097172564",
          |    "dueDate": "2015-01-31",
          |    "amount": 2300,
          |    "taxYear": {
          |      "start": 2014,
          |      "end": 2015
          |    }
          | }
          |]
          |}""".stripMargin
      val request: WSRequest = wsUrl(
        s"/register/secure-messages?journeyId=$journeyId"
      ).addHttpHeaders(acceptJsonHeader, sandboxHeader)
      val response = await(request.get(Json.parse(body)))
      response.status shouldBe 400
    }
  }

  "when payload contains no nextPayment property" should {
    "return 400" in {
      grantAccess()
      val body =
        """{
          | "accountSummary": {
          |    "totalAmountDueToHmrc": {
          |      "amount": "12345.67",
          |      "requiresPayment": "true"
          |    },
          |    "amountHmrcOwe": "0"
          |    },
          | "futureLiability": [
          | {
          |    "descriptionCode": "BCD",
          |    "dueDate": "2015-01-31",
          |    "amount": "503.2",
          |    "taxYear": {
          |      "start": "2014",
          |      "end": "2015"
          |    },
          | {
          |    "descriptionCode": "IN1",
          |    "partnershipReference": "1097172564",
          |    "dueDate": "2015-01-31",
          |    "amount": 2300,
          |    "taxYear": {
          |      "start": 2014,
          |      "end": 2015
          |    }
          | }
          |]
          |}""".stripMargin
      val request: WSRequest = wsUrl(
        s"/register/secure-messages?journeyId=$journeyId"
      ).addHttpHeaders(acceptJsonHeader)
      val response = await(request.get(Json.parse(body)))
      response.status shouldBe 400
    }
  }
}