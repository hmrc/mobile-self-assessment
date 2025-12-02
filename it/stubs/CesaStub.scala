package stubs

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, stubFor, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import uk.gov.hmrc.mobileselfassessment.model.SaUtr

import java.time.{LocalDate, ZoneId}

object CesaStub {

  def stubForGetRootLinks(
    utr: SaUtr,
    response: String
  ): StubMapping =
    stubFor(
      get(
        urlEqualTo(
          s"/self-assessment/individual/$utr"
        )
      ).willReturn(
        aResponse()
          .withStatus(200)
          .withBody(response)
      )
    )

  def stubForGetRootLinksFailure(
    utr: SaUtr,
    status: Int
  ): StubMapping =
    stubFor(
      get(
        urlEqualTo(
          s"/self-assessment/individual/$utr"
        )
      ).willReturn(
        aResponse()
          .withStatus(status)
      )
    )

  def stubForGetAccountSummary(
    utr: SaUtr,
    response: String
  ): StubMapping =
    stubFor(
      get(
        urlEqualTo(
          s"/self-assessment/individual/$utr/account-summary"
        )
      ).willReturn(
        aResponse()
          .withStatus(200)
          .withBody(response)
      )
    )

  def stubForGetFutureLiabilities(
    utr: SaUtr,
    response: String
  ): StubMapping =
    stubFor(
      get(
        urlEqualTo(
          s"/self-assessment/individual/$utr/account/futureliabilities"
        )
      ).willReturn(
        aResponse()
          .withStatus(200)
          .withBody(response)
      )
    )

  def stubForGetFutureLiabilitiesViaHip(
    utr: SaUtr,
    response: String,
    status: Int = 200
  ): StubMapping = {
    val currentDate = LocalDate.now(ZoneId.of("Europe/London"))
    stubFor(
      get(
        urlEqualTo(
          s"/as/self-assessment/account/$utr/liability-details?dateFrom=${currentDate.minusYears(7).toString}&dateTo=${currentDate.toString}"
        )
      ).willReturn(
        aResponse()
          .withStatus(status)
          .withBody(response)
      )
    )
  }

  def stubForGetFutureLiabilitiesViaHipError(
                                         utr: SaUtr,
                                         status: Int
                                       ): StubMapping = {
    val currentDate = LocalDate.now(ZoneId.of("Europe/London"))
    stubFor(
      get(
        urlEqualTo(
          s"/as/self-assessment/account/$utr/liability-details?dateFrom=${currentDate.minusYears(7).toString}&dateTo=${currentDate.toString}"
        )
      ).willReturn(
        aResponse()
          .withStatus(status)
          
      )
    )
  }

}
