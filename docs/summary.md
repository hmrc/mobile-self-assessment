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
    "taxToPayStatus": "OnlyBill",
    "totalAmountDueToHmrc": {
      "amount": 0,
      "requiresPayment": true
    },
    "amountHmrcOwe": 0,
    "totalFutureLiability": 2000,
    "nextBill": {
      "dueDate": "2022-01-31",
      "amount": 1150,
      "daysRemaining": 20
    }
  },
  "futureLiability": [{
    "dueDate": "2022-01-31",
    "futureLiabilities": [{
      "descriptionCode": "IN1",
      "descriptionText": "First payment on account for 2021 to 2022",
      "dueDate": "2022-01-31",
      "amount": 850,
      "taxYear": {
        "start": 2021,
        "end": 2022
      }
    }, {
      "descriptionCode": "BCD",
      "descriptionText": "Balancing payment for 2021 to 2022",
      "dueDate": "2022-01-31",
      "amount": 300,
      "taxYear": {
        "start": 2021,
        "end": 2022
      }
    }],
    "total": 1150
  }, {
    "dueDate": "2022-07-31",
    "futureLiabilities": [{
      "descriptionCode": "IN2",
      "descriptionText": "First payment on account for 2021 to 2022",
      "dueDate": "2022-07-31",
      "amount": 850,
      "taxYear": {
        "start": 2021,
        "end": 2022
      }
    }],
    "total": 850
    }
  ],
  "setUpPaymentPlanUrl": "/",
  "updateOrSubmitAReturnUrl": "/",
  "viewPaymentHistoryUrl": "/",
  "viewOtherYearsUrl": "/",
  "moreSelfAssessmentDetailsUrl": "/",
  "payByDebitOrCardPaymentUrl": "/",
  "claimRefundUrl": "/"
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
  "claimRefundUrl": "/"
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



