/*
 * Copyright 2026 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.{any, any as URL}
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.HeaderCarrier
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.libs.json.Json

import java.net.URL
import uk.gov.hmrc.mobileselfassessment.MobileSelfAssessmentTestData
import uk.gov.hmrc.mobileselfassessment.config.AppConfig
import uk.gov.hmrc.mobileselfassessment.model.SaUtr
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import play.api.test.Helpers.await
import play.api.test.Helpers.defaultAwaitTimeout
import uk.gov.hmrc.mobileselfassessment.cesa.{CesaAccountSummary, CesaRootLinks, CesaRootLinksWrapper}
import uk.gov.hmrc.mobileselfassessment.mocks.BaseMock

import scala.concurrent.Future

class CesaIndividualsConnectorSpec
    extends AnyWordSpec
    with MockitoSugar
    with ScalaFutures
    with GuiceOneAppPerSuite
    with MobileSelfAssessmentTestData
    with BaseMock {

  val appConfig = new AppConfig(config, new ServicesConfig(config))

  val mockHttp: HttpClientV2 = mock[HttpClientV2]
  val mockRequestBuilder = mock[RequestBuilder]

  val cesaConnector = new CesaIndividualsConnector(mockHttp, appConfig)
  val hipCesaConnector = new CesaIndividualsConnector(mockHttp, appConfig, true)
  private val utr: SaUtr = SaUtr("UTR123")

  "CesaIndividualsConnectorSpec" should {

    "return cesa account summary data if 200 response is received" when {

      "DWIT flag is off" in {

        val accountSummary: CesaAccountSummary = Json.parse(accountSummaryResponse).as[CesaAccountSummary]
        when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Option[CesaAccountSummary]](using any, any)).thenReturn(Future.successful(Some(accountSummary)))
        val result = await(cesaConnector.getOptionalCesaAccountSummary(utr, s"/self-assessment/individual/$utr/account-summary"))
        result shouldBe Some(accountSummary)
      }
      "DWIT flag is on" in {

        val accountSummary: CesaAccountSummary = Json.parse(accountSummaryResponse).as[CesaAccountSummary]
        when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Option[CesaAccountSummary]](using any, any)).thenReturn(Future.successful(Some(accountSummary)))
        val result = await(hipCesaConnector.getOptionalCesaAccountSummary(utr, s"/ods-sa/v1/self-assessment/individual/$utr/account-summary"))
        result shouldBe Some(accountSummary)
      }
    }

    "return cesa Future liability data if 200 response is received" when {

      "DWIT flag is off" in {

        val accountSummary: CesaAccountSummary = Json.parse(accountSummaryResponse).as[CesaAccountSummary]
        when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Option[CesaAccountSummary]](using any, any)).thenReturn(Future.successful(Some(customFutureLiabilities(800))))
        val result = await(cesaConnector.futureLiabilities(utr))
        result shouldBe (customFutureLiabilities(800))
      }
      "DWIT flag is on" in {

        val accountSummary: CesaAccountSummary = Json.parse(accountSummaryResponse).as[CesaAccountSummary]
        when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Option[CesaAccountSummary]](using any, any)).thenReturn(Future.successful(Some(customFutureLiabilities(800))))
        val result = await(hipCesaConnector.futureLiabilities(utr))
        result shouldBe (customFutureLiabilities(800))
      }
    }

    "return cesa root links if 200 response is received" when {
      val cesaRootLinks = CesaRootLinks(accountSummary = Some("/account-summary"))
      val cesaRootLinksWrapper = CesaRootLinksWrapper(links = cesaRootLinks)

      "DWIT flag is off" in {

        when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[CesaRootLinks](using any, any)).thenReturn(Future.successful(cesaRootLinksWrapper))
        val result = await(cesaConnector.getRootLinks(utr))
        result shouldBe cesaRootLinks
      }
      "DWIT flag is on" in {

        when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Option[CesaAccountSummary]](using any, any)).thenReturn(Future.successful(cesaRootLinksWrapper))
        val result = await(hipCesaConnector.getRootLinks(utr))
        result shouldBe cesaRootLinks
      }
    }
  }
}
