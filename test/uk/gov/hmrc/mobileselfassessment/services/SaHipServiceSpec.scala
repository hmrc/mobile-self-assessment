package uk.gov.hmrc.mobileselfassessment.services
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.mobileselfassessment.MobileSelfAssessmentTestData
import uk.gov.hmrc.mobileselfassessment.cesa.{CesaAccountSummary, CesaAmount, CesaFutureLiability, CesaRootLinks}
import uk.gov.hmrc.mobileselfassessment.connectors.{CesaIndividualsConnector, HipConnector}
import uk.gov.hmrc.mobileselfassessment.model.{BCD, GetLiabilitiesResponse, IN1, IN2, SaUtr}

import scala.concurrent.{ExecutionContext, Future}

class SaHipServiceSpec extends AnyWordSpec with MobileSelfAssessmentTestData with Matchers with MockFactory with FutureAwaits with DefaultAwaitTimeout {

    implicit lazy val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    implicit lazy val hc: HeaderCarrier = HeaderCarrier()
    val spreadCostUrl = "http://spread-cost-url"

    implicit val mockCesaConnector: CesaIndividualsConnector = mock[CesaIndividualsConnector]
    private val service = new SaService(mockCesaConnector)
}