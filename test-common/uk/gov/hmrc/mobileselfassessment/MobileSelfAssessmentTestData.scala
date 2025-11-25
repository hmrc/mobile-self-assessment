/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.mobileselfassessment

import play.api.libs.json.Json
import uk.gov.hmrc.mobileselfassessment.hip.{HipError, HipErrorDetails, HipResponseError}
import uk.gov.hmrc.mobileselfassessment.model.{BalanceDetails, ChargeDetails, HipResponse, SaUtr}

import java.time.LocalDate

trait MobileSelfAssessmentTestData {

  val hipResponse = HipResponse(
    balanceDetails = BalanceDetails(1156.37, 0),
    chargeDetails = List(
      ChargeDetails("ACI", 15962.3, "2018-2019", LocalDate.of(2023, 10, 12)),
      ChargeDetails("ACI", 31167.10, "2018-2019", LocalDate.of(2024, 11, 12)),
      ChargeDetails("ACI", 15723.01, "2017-2018", LocalDate.of(2025, 1, 1))
    )
  )

  def getRootLinksResponse(utr: SaUtr): String =
    s"""
       |{
       |"links": {
       |  "accountSummary": "/self-assessment/individual/$utr/account-summary"
       |  }
       |}
       |""".stripMargin

  def getRootLinksNoAccountSummaryResponse(utr: SaUtr): String =
    s"""
       |{
       |"links": {
       |  "designatoryDetails": "/self-assessment/individual/$utr/designatory-details",
       |  "calendar": "/self-assessment/individual/$utr/calendar"
       |  }
       |}
       |""".stripMargin

  val getRootLinksAccountSummaryNullResponse: String =
    s"""
       |{
       |"links": {
       |  "accountSummary": null
       |  }
       |}
       |""".stripMargin

  val accountSummaryResponse: String =
    s"""
       |{
       |"totalAmountDueToHmrc": {
       |  "amount": 12345.67,
       |  "currency": "GBP"
       |  },
       |"nextPayment": {
       |  "paymentDueDate": "2014-01-31",
       |  "amount": {
       |    "amount": 12345.67,
       |    "currency": "GBP"
       |    }
       |  },
       |"amountHmrcOwe": {
       |  "amount": 0,
       |  "currency": "GBP"
       |  }
       |}
       |""".stripMargin

  val accountSummaryNothingOwedResponse: String =
    s"""
       |{
       |"totalAmountDueToHmrc": {
       |  "amount": 0,
       |  "currency": "GBP"
       |  },
       |"nextPayment": {
       |  "amount": {
       |    "amount": 0,
       |    "currency": "GBP"
       |    }
       |  },
       |"amountHmrcOwe": {
       |  "amount": 0,
       |  "currency": "GBP"
       |  }
       |}
       |""".stripMargin

  val accountSummaryResponseInvalidCurrency: String =
    s"""
       |{
       |"totalAmountDueToHmrc": {
       |  "amount": 12345.67,
       |  "currency": "EUR"
       |  },
       |"nextPayment": {
       |  "paymentDueDate": "2014-01-31",
       |  "amount": {
       |    "amount": 12345.67,
       |    "currency": "EUR"
       |    }
       |  },
       |"amountHmrcOwe": {
       |  "amount": 0,
       |  "currency": "EUR"
       |  }
       |}
       |""".stripMargin

  val accountSummaryResponseNullValue: String =
    s"""
       |{
       |"totalAmountDueToHmrc": {
       |  "amount": 12345.67,
       |  "currency": "GBP"
       |  },
       |"nextPayment": {
       |  "paymentDueDate": "2014-01-31",
       |  "amount": {
       |    "amount": 12345.67,
       |    "currency": "GBP"
       |    }
       |  },
       |"amountHmrcOwe": {
       |  "amount": null,
       |  "currency": "GBP"
       |  }
       |}
       |""".stripMargin

  val accountSummaryMalformedResponse: String =
    s"""
       |{
       |"totalAmountDueToHmrc": {
       |  "amount": 12345.67,
       |  "currency": "GBP
       |  },
       |"nextPayment": {
       |  "paymentDueDate": "2014-01-31",
       |  "amount": {
       |    "amount": 12345.67,
       |    "currency": "GBP"
       |    }
       |  } }
       |"amountHmrcOwe": {
       |  "amount": 0,
       |  "currency": "GBP"
       |  }
       |}
       |""".stripMargin

  val futureLiabilitiesResponse: String =
    s"""
       |[
       |    {
       |        "taxYearEndDate": "2015-04-05",
       |        "descriptionCode": "JEP",
       |        "partnershipReference": 0,
       |        "statutoryDueDate": "2015-01-31",
       |        "relevantDueDate": "2015-01-31",
       |        "amount": {
       |            "amount": 503.20,
       |            "currency": "GBP"
       |        },
       |        "taxYear": "1415",
       |        "transactionId": {
       |            "tieBreaker": 4828,
       |            "sequenceNumber": null,
       |            "creationDate": "2014-04-07"
       |        },
       |        "penaltyRegimeChangeDescriptor": null,
       |        "links": [
       |            {
       |                "rel": "self",
       |                "href": "https://digital.ws.ibt.hmrc.gov.uk/self-assessment/individual/1121766916/account/futureliabilities"
       |            }
       |        ]
       |    },
       |    {
       |        "taxYearEndDate": "2015-04-05",
       |        "descriptionCode": "PP2",
       |        "partnershipReference": 1097172564,
       |        "statutoryDueDate": "2015-01-31",
       |        "relevantDueDate": "2015-01-31",
       |        "amount": {
       |            "amount": 2300.00,
       |            "currency": "GBP"
       |        },
       |        "taxYear": "1415",
       |        "transactionId": {
       |            "tieBreaker": 4829,
       |            "sequenceNumber": null,
       |            "creationDate": "2014-04-07"
       |        },
       |        "penaltyRegimeChangeDescriptor": null,
       |        "links": [
       |            {
       |                "rel": "self",
       |                "href": "https://digital.ws.ibt.hmrc.gov.uk/self-assessment/individual/1121766916/account/futureliabilities"
       |            }
       |        ]
       |    },
       |    {
       |        "taxYearEndDate": "2015-04-05",
       |        "descriptionCode": "PP2",
       |        "partnershipReference": 1097172564,
       |        "statutoryDueDate": "2016-01-31",
       |        "relevantDueDate": "2016-01-31",
       |        "amount": {
       |            "amount": 2300.00,
       |            "currency": "GBP"
       |        },
       |        "taxYear": "1415",
       |        "transactionId": {
       |            "tieBreaker": 4829,
       |            "sequenceNumber": null,
       |            "creationDate": "2014-04-07"
       |        },
       |        "penaltyRegimeChangeDescriptor": null,
       |        "links": [
       |            {
       |                "rel": "self",
       |                "href": "https://digital.ws.ibt.hmrc.gov.uk/self-assessment/individual/1121766916/account/futureliabilities"
       |            }
       |        ]
       |    },
       |    {
       |        "taxYearEndDate": "2015-04-05",
       |        "descriptionCode": "PP2",
       |        "partnershipReference": 1097172564,
       |        "statutoryDueDate": "2016-01-31",
       |        "relevantDueDate": "2016-01-31",
       |        "amount": {
       |            "amount": 2300.00,
       |            "currency": "GBP"
       |        },
       |        "taxYear": "1415",
       |        "transactionId": {
       |            "tieBreaker": 4829,
       |            "sequenceNumber": null,
       |            "creationDate": "2014-04-07"
       |        },
       |        "penaltyRegimeChangeDescriptor": null,
       |        "links": [
       |            {
       |                "rel": "self",
       |                "href": "https://digital.ws.ibt.hmrc.gov.uk/self-assessment/individual/1121766916/account/futureliabilities"
       |            }
       |        ]
       |    },
       |    {
       |        "taxYearEndDate": "2015-04-05",
       |        "descriptionCode": "PP2",
       |        "partnershipReference": 1097172564,
       |        "statutoryDueDate": "2016-06-28",
       |        "relevantDueDate": "2016-06-28",
       |        "amount": {
       |            "amount": 2300.00,
       |            "currency": "GBP"
       |        },
       |        "taxYear": "1415",
       |        "transactionId": {
       |            "tieBreaker": 4829,
       |            "sequenceNumber": null,
       |            "creationDate": "2014-04-07"
       |        },
       |        "penaltyRegimeChangeDescriptor": null,
       |        "links": [
       |            {
       |                "rel": "self",
       |                "href": "https://digital.ws.ibt.hmrc.gov.uk/self-assessment/individual/1121766916/account/futureliabilities"
       |            }
       |        ]
       |    }
       |]
       |""".stripMargin

  val hipResponseJsonMalformed =
    """
      |{
      |  "balanceDetails": {
      |    "totalBalance": 12345.67,
      |    "totalCreditAvailable": 0
      |  }}
      |""".stripMargin
  val hip404Error = Json
    .toJson(HipResponseError("hip", None, HipErrorDetails(List(HipError("Not Found", "badMessage")))))
    .toString
  val hipResponseJson1: String =
    """
         {
        |  "balanceDetails": {
        |    "totalBalance": 12345.67,
        |    "totalCreditAvailable": 0
        |  },
        |  "chargeDetails": [
        |    {
        |      "chargeType": "JEP",
        |      "outstandingAmount": 503.20,
        |      "taxYear": "2014",
        |      "dueDate": "2015-01-31"
        |    },
        |    {
        |      "chargeType": "PP2",
        |      "outstandingAmount": 2300.00,
        |      "taxYear": "2014",
        |      "dueDate": "2015-01-31"
        |    },
        |    {
        |      "chargeType": "PP2",
        |      "outstandingAmount": 2300.00,
        |      "taxYear": "2014",
        |      "dueDate": "2016-01-31"
        |    },
        |{
        |      "chargeType": "PP2",
        |      "outstandingAmount": 2300.00,
        |      "taxYear": "2014",
        |      "dueDate": "2016-06-28"
        |    }
        |  ]
        |}
        |""".stripMargin

  val hipResponseJsonNoFutureLiability: String =
    """
           {
      |  "balanceDetails": {
      |    "totalBalance": 12345.67,
      |    "totalCreditAvailable": 0
      |  },
      |  "chargeDetails": []
      |}
      |""".stripMargin

  val getLiabilitiesResponse: String =
    s"""
       |{
       |  "accountSummary": {
       |    "taxToPayStatus": "OverdueWithBill",
       |    "totalAmountDueToHmrc": {
       |      "amount": 12345.67,
       |      "requiresPayment": true
       |    },
       |    "nextBill": {
       |      "dueDate": "2015-01-31",
       |      "amount": 2803.20,
       |      "daysRemaining": -1
       |    },
       |    "amountHmrcOwe": 0,
       |    "totalLiability": 2803.20
       |  },
       |  "futureLiability": [
       |    {
       |      "dueDate": "2015-01-31",
       |      "futureLiabilities": [ {
       |        "descriptionCode": "BCD",
       |        "dueDate": "2015-01-31",
       |        "amount": 503.2,
       |        "taxYear": {
       |          "start": 2014,
       |          "end": 2015
       |        }
       |      },
       |      {
       |        "descriptionCode": "IN1",
       |        "partnershipReference": "1097172564",
       |        "dueDate": "2015-01-31",
       |        "amount": 2300,
       |        "taxYear": {
       |          "start": 2014,
       |          "end": 2015
       |        }
       |      } ],
       |      "total": 2803.2
       |   }
       |  ],
       |  "setUpPaymentPlanUrl": "/pay-what-you-owe-in-instalments/arrangement/determine-eligibility",
       |  "updateOrSubmitAReturnUrl": "/personal-account/self-assessment-summary",
       |  "viewPaymentHistoryUrl":  "/self-assessment/ind/123123123/account/payments",
       |  "viewOtherYearsUrl": "/self-assessment/ind/123123123/account/taxyear/2122",
       |  "moreSelfAssessmentDetailsUrl":  "/self-assessment/ind/123123123/account",
       |  "payByDebitOrCardPaymentUrl": "/personal-account/self-assessment-summary",
       |  "claimRefundUrl": "/contact/self-assessment/ind/123123123/repayment",
       |  "spreadCostUrl": "/personal-account/sa/spread-the-cost-of-your-self-assessment"
       |}
       |""".stripMargin

}
