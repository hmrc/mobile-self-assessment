package stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping

object ShutteringStub {

  def stubForShutteringDisabled: StubMapping =
    stubFor(
      get(
        urlEqualTo(
          s"/mobile-shuttering/service/mobile-self-assessment/shuttered-status?journeyId=27085215-69a4-4027-8f72-b04b10ec16b0"
        )
      ).willReturn(
          aResponse()
            .withStatus(200)
            .withBody(s"""
                         |{
                         |  "shuttered": false,
                         |  "title":     "",
                         |  "message":    ""
                         |}
          """.stripMargin)
        )
    )

  def stubForShutteringEnabled: StubMapping =
    stubFor(
      get(
        urlEqualTo(
          s"/mobile-shuttering/service/mobile-self-assessment/shuttered-status?journeyId=27085215-69a4-4027-8f72-b04b10ec16b0"
        )
      ).willReturn(
          aResponse()
            .withStatus(200)
            .withBody(s"""
                         |{
                         |  "shuttered": true,
                         |  "title":     "Shuttered",
                         |  "message":   "Self-Assessment is currently not available"
                         |}
          """.stripMargin)
        )
    )

}
