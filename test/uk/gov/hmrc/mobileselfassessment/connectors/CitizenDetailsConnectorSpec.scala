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

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.mobileselfassessment.config.AppConfig
import uk.gov.hmrc.mobileselfassessment.MobileSelfAssessmentTestData
import play.api.Configuration
import com.typesafe.config.ConfigFactory
import org.mockito.ArgumentMatchers.{any, any as URL}
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.test.Helpers.defaultAwaitTimeout
import uk.gov.hmrc.domain.{Nino, SaUtr, TaxIds}
import uk.gov.hmrc.mobileselfassessment.model.CidPerson

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

class CitizenDetailsConnectorSpec extends AnyWordSpec with MockitoSugar with ScalaFutures with GuiceOneAppPerSuite with MobileSelfAssessmentTestData {

  val config = Configuration(
    ConfigFactory.parseString(s"""
         |microservice.services.hip.host=localhost
         |microservice.services.hip.port=9718
         |microservice.services.citizen-details.host=localhost
         |microservice.services.citizen-details.port=9337
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
  val connector = new CitizenDetailsConnector(mockHttp, appConfig)
  val nino = "AA000003D"

  val user: CidPerson = CidPerson(TaxIds(Set(Nino("AA000003D"), SaUtr("1097133333"))))

  "CitizenDetailsConnectorSpec" should {

    "return 200 and fetch UTR via Nino if present" in {
      when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[CidPerson](using any, any)).thenReturn(Future.successful(user))
      val result = await(connector.getUtrByNino(nino))
      result shouldBe Some(SaUtr("1097133333"))
    }

    "return None if citizen details api fail with Upstream error response of bad request" in {
      when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[CidPerson](using any, any)).thenReturn(Future.failed(UpstreamErrorResponse("bad requet", 400)))
      val result = await(connector.getUtrByNino(nino))
      result shouldBe None
    }

    "return None if citizen details api fail with any Upstream error response" in {
      when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[CidPerson](using any, any)).thenReturn(Future.failed(UpstreamErrorResponse("Server error", 500)))
      val result = await(connector.getUtrByNino(nino))
      result shouldBe None
    }

    "throw exception, if the UTR is not found" in {
      when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[CidPerson](using any, any)).thenReturn(Future.failed(UpstreamErrorResponse("UTR not found", 404)))
      val result = connector.getUtrByNino(nino)
      intercept[NotFoundException] {
        await(result)
      }

    }
    "throw Not exception, if citizen details api throw nit found exception" in {
      when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[CidPerson](using any, any)).thenReturn(Future.failed(NotFoundException("UTR not found")))
      val result = connector.getUtrByNino(nino)
      intercept[NotFoundException] {
        await(result)
      }

    }

  }

}
