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

package uk.gov.hmrc.mobileselfassessment

import uk.gov.hmrc.mobileselfassessment.model.SaUtr

trait MobileSelfAssessmentTestData {

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
       |    }
       |]
       |""".stripMargin

  val getLiabilitiesResponse: String =
    s"""
       |{
       |  "accountSummary": {
       |    "taxToPayStatus": "OverDue",
       |    "totalAmountDueToHmrc": {
       |      "amount": 12345.67,
       |      "requiresPayment": true
       |    },
       |    "nextPayment": {
       |      "dueDate": "2014-01-31",
       |      "amount": 12345.67
       |    },
       |    "amountHmrcOwe": 0
       |  },
       |  "futureLiabilities" :
       |  [
       |      {
       |          "taxYearEndDate": "2015-04-05",
       |          "descriptionCode": "LFI2",
       |          "partnershipReference": 0,
       |          "statutoryDueDate": "2015-01-31",
       |          "relevantDueDate": "2015-01-31",
       |          "amount": {
       |              "amount": 503.20,
       |              "currency": "GBP"
       |          },
       |          "taxYear": "1415",
       |          "transactionId": {
       |              "tieBreaker": 4828,
       |              "sequenceNumber": null,
       |              "creationDate": "2014-04-07"
       |          },
       |          "penaltyRegimeChangeDescriptor": null,
       |          "links": [
       |              {
       |                  "rel": "self",
       |                  "href": "https://digital.ws.ibt.hmrc.gov.uk/self-assessment/individual/1121766916/account/futureliabilities"
       |              }
       |          ]
       |      },
       |      {
       |          "taxYearEndDate": "2015-04-05",
       |          "descriptionCode": "LPP1",
       |          "partnershipReference": 1097172564,
       |          "statutoryDueDate": "2015-01-31",
       |          "relevantDueDate": "2015-01-31",
       |          "amount": {
       |              "amount": 2300.00,
       |              "currency": "GBP"
       |          },
       |          "taxYear": "1415",
       |          "transactionId": {
       |              "tieBreaker": 4829,
       |              "sequenceNumber": null,
       |              "creationDate": "2014-04-07"
       |          },
       |          "penaltyRegimeChangeDescriptor": null,
       |          "links": [
       |              {
       |                  "rel": "self",
       |                  "href": "https://digital.ws.ibt.hmrc.gov.uk/self-assessment/individual/1121766916/account/futureliabilities"
       |              }
       |          ]
       |      }
       |  ],
       |  "setUpPaymentPlanUrl": "/pay-what-you-owe-in-instalments/arrangement/determine-eligibility",
       |  "updateOrSubmitAReturnUrl": "/personal-account/self-assessment-summary",
       |  "viewPaymentHistoryUrl":  "/self-assessment/ind/123123123/account/payments",
       |  "viewOtherYearsUrl": "/self-assessment/ind/123123123/account/taxyear/2122",
       |  "moreSelfAssessmentDetailsUrl":  "/self-assessment/ind/123123123/account",
       |  "payByDebitOrCardPaymentUrl": "/personal-account/self-assessment-summary"
       |}
       |""".stripMargin

}
