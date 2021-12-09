The Get Liabilities response
----
Fetch the GetLiabilities object.

* **URL**

  `/mobile-self-assessment/:utr/liabilities`

* **Method:**

  `GET`

* **URL Params**

  **Required:**

  `journeyId=[String]`

  a string which is included for journey tracking purposes but has no functional impact

* **Success Responses:**

    * **Code:** 200 <br />
      **Content:** Full Response

```json
{
  "accountSummary": {
    "taxToPayStatus": "CreditLessThanBill",
    "totalAmountDueToHmrc": {
      "amount": 12345.67,
      "requiresPayment": true
    },
    "nextBill": {
      "dueDate": "2015-01-31",
      "daysRemaining": 25,
      "amount": 2803.20
    },
    "amountHmrcOwe": 2000,
    "totalFutureLiability": 2803.20,
    "remainingAfterCreditDeducted": 803.20
  },
  "futureLiability": [
    {
      "descriptionCode": "BCD",
      "descriptionText": "Balancing payment for 2015 to 2016",
      "dueDate": "2015-01-31",
      "amount": 503.2,
      "taxYear": {
        "start": 2014,
        "end": 2015
      }
    },
    {
      "descriptionCode": "IN1",
      "descriptionText": "First payment on account for 2015 to 2016",
      "partnershipReference": "1097172564",
      "dueDate": "2015-01-31",
      "amount": 2300,
      "taxYear": {
        "start": 2014,
        "end": 2015
      }
    }
  ],
  "setUpPaymentPlanUrl": "/",
  "updateOrSubmitAReturnUrl": "/",
  "viewPaymentHistoryUrl": "/",
  "viewOtherYearsUrl": "/",
  "moreSelfAssessmentDetailsUrl": "/",
  "payByDebitOrCardPaymentUrl": "/",
  "claimRefundUrl": "/",
  "viewBreakdownUrl": "/"
}
```

* **Code:** 200 <br />
  **Content:** No future liabilities

```json
{
  "accountSummary": {
    "taxToPayStatus": "Overdue",
    "totalAmountDueToHmrc": {
      "amount": 12345.67,
      "requiresPayment": true
    },
    "amountHmrcOwe": 0
  },
  "setUpPaymentPlanUrl": "/",
  "updateOrSubmitAReturnUrl": "/",
  "viewPaymentHistoryUrl": "/",
  "viewOtherYearsUrl": "/",
  "moreSelfAssessmentDetailsUrl": "/",
  "payByDebitOrCardPaymentUrl": "/",
  "claimRefundUrl": "/",
  "viewBreakdownUrl": "/"
}
```

* **Error Responses:**

    * **Code:** 401 UNAUTHORIZED <br/>
      **Content:** `{"code":"UNAUTHORIZED","message":"Bearer token is missing or not authorized for access"}`

    * **Code:** 404 NOT_FOUND <br/>

    * **Code:** 406 NOT_ACCEPTABLE <br/>
      **Content:** `{"code":"NOT_ACCEPTABLE","message":Missing Accept Header"}`

  OR when a user does not exist or server failure

    * **Code:** 500 INTERNAL_SERVER_ERROR <br/>

    * **Code:** 521 SHUTTERED <br/>
      **Content:** ```{
      "shuttered": true,
      "title": "Service Unavailable",
      "message": "You’ll be able to use the SA service at 9am on Monday 29 May 2017."
      }```



