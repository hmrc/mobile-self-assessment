package stubs

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, stubFor, urlEqualTo}
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import uk.gov.hmrc.mobileselfassessment.model.SaUtr

object CesaStub {

  def stubForGetRootLinks(
    utr:      SaUtr,
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
    utr:    SaUtr,
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
    utr:      SaUtr,
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
    utr:      SaUtr,
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

}
