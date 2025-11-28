/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.mobileselfassessment.connectors

import com.typesafe.config.ConfigFactory
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException}
import uk.gov.hmrc.mobileselfassessment.model.HipResponse
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.libs.json.{JsValue, Json}

import java.net.URL
import uk.gov.hmrc.mobileselfassessment.MobileSelfAssessmentTestData
import uk.gov.hmrc.mobileselfassessment.config.AppConfig
import uk.gov.hmrc.mobileselfassessment.model.SaUtr
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import play.api.test.Helpers.await
import play.api.test.Helpers.defaultAwaitTimeout
import uk.gov.hmrc.mobileselfassessment.hip.{HipError, HipErrorDetails, HipExceptions, HipResponseError}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class HipConnectorSpec extends AnyWordSpec with MockitoSugar with ScalaFutures with GuiceOneAppPerSuite with MobileSelfAssessmentTestData {

  val config = Configuration(
    ConfigFactory.parseString(s"""
         |microservice.services.hip.host=localhostHip
         |microservice.services.hip.port=9718
         |microservice.services.hip.clientId=clientId
         |microservice.services.hip.clientSecret=clientSecret
         |microservice.services.cesa.host=localhostCesa
         |microservice.services.cesa.port=9719
         |""".stripMargin)
  )

  val appConfig = new AppConfig(config, new ServicesConfig(config))
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockHttp: HttpClientV2 = mock[HttpClientV2]
  val mockRequestBuilder = mock[RequestBuilder]

  val connector = new HipConnector(mockHttp, appConfig)
  private val utr: SaUtr = SaUtr("UTR123")

  val hipResponseJson: JsValue =
    Json.parse("""
       {
       |  "balanceDetails": {
       |    "totalBalance": 1156.37,
       |    "totalCreditAvailable": 0
       |  },
       |  "chargeDetails": [
       |    {
       |      "chargeType": "ACI",
       |      "outstandingAmount": 15962.3,
       |      "taxYear": "2018-2019",
       |      "dueDate": "2023-10-12"
       |    },
       |    {
       |      "chargeType": "ACI",
       |      "outstandingAmount": 31167.10,
       |      "taxYear": "2018-2019",
       |      "dueDate": "2024-11-12"
       |    },
       |    {
       |      "chargeType": "ACI",
       |      "outstandingAmount": 15723.01,
       |      "taxYear": "2017-2018",
       |      "dueDate": "2025-01-01"
       |    }
       |  ]
       |}
       |""".stripMargin)

  val hipResponseMalformedJson: JsValue =
    Json.parse("""
         {
          |  "balanceDetails": {
          |    "totalCreditAvailable": 0
          |  }
          |
          |}
          |""".stripMargin)

  private val hipError = Json
    .toJson(HipResponseError("hip", None, HipErrorDetails(List(HipError("badType", "badMessage")))))
    .toString
  private val hipErrorMalformed: JsValue = Json.parse("""
      {
                                                        |  "origin": "hip",
                                                        |  "response": {}
                                                        |}
      |""".stripMargin)

  "HipConnectorSpec" should {

    "get SelfAssessment Liabilities Data" should {

      "return JSON associated with the utr and date if 200 response is received" in {
        when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.transform(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[HttpResponse](using any, any)).thenReturn(Future.successful(HttpResponse(200, hipResponseJson.toString)))
        val result = await(connector.getSelfAssessmentLiabilitiesData(utr))

        result shouldBe hipResponse
      }

      "return expected error if returned JSON is malformed" in {

        when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.transform(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[HttpResponse](using any, any))
          .thenReturn(Future.successful(HttpResponse(200, hipResponseMalformedJson.toString)))
        val result: Future[HipResponse] = connector.getSelfAssessmentLiabilitiesData(utr)
        result.failed.futureValue shouldBe (HipExceptions(s"Unable to retrieve data: SA (Individual) root data for UTR '$utr' via HIP"))

      }

      "return expected error if 400 response is received" in {

        when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.transform(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[HttpResponse](using any, any))
          .thenReturn(Future.successful(HttpResponse(400, hipError)))
        val result: Future[HipResponse] = connector.getSelfAssessmentLiabilitiesData(utr)
        result.failed.futureValue shouldBe (HipExceptions(s"Error from downstream systems"))
      }

      "return expected error if 403 response is received" in {

        when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.transform(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[HttpResponse](using any, any))
          .thenReturn(Future.successful(HttpResponse(403, hipError)))
        val result: Future[HipResponse] = connector.getSelfAssessmentLiabilitiesData(utr)
        result.failed.futureValue shouldBe (HipExceptions(s"Error from downstream systems"))
      }

      "return expected error if 404 response is received" in {

        when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.transform(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[HttpResponse](using any, any))
          .thenReturn(Future.successful(HttpResponse(404, hipError)))
        val result: Future[HipResponse] = connector.getSelfAssessmentLiabilitiesData(utr)

        intercept[NotFoundException] {
          await(result)
        }
      }

      "return expected error if 422 response is received" in {

        when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.transform(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[HttpResponse](using any, any))
          .thenReturn(Future.successful(HttpResponse(422, hipError)))
        val result: Future[HipResponse] = connector.getSelfAssessmentLiabilitiesData(utr)
        intercept[NotFoundException] {
          await(result)
        }
      }

      "return expected error if 500 response is received" in {

        when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.transform(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[HttpResponse](using any, any))
          .thenReturn(Future.successful(HttpResponse(500, hipError)))
        val result: Future[HipResponse] = connector.getSelfAssessmentLiabilitiesData(utr)
        result.failed.futureValue shouldBe (HipExceptions(s"Error from downstream systems"))
      }

      "return expected error if 500 response is received with error json malformed" in {

        when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.transform(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[HttpResponse](using any, any))
          .thenReturn(Future.successful(HttpResponse(500, hipErrorMalformed.toString)))
        val result: Future[HipResponse] = connector.getSelfAssessmentLiabilitiesData(utr)
        result.failed.futureValue shouldBe (HipExceptions(s"Json Validation error"))
      }
    }
  }
}
