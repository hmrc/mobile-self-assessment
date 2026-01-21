package stubs

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson, get, stubFor, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping

object CitizenDetailsStub {

  def stubForGetUTRViaNino(
    nino: String,
    utr: String
  ): StubMapping =
    stubFor(
      get(
        urlEqualTo(
          s"/citizen-details/nino/$nino"
        )
      ).willReturn(
        aResponse()
          .withStatus(200)
          .withBody(s"""
                       |{
                       |  "ids": {"sautr" : "$utr"}
                       |}
                     """.stripMargin)
      )
    )
}
