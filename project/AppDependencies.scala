import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  private val bootstrapPlay28Version = "5.12.0"
  private val playHmrcApiVersion     = "6.4.0-play-28"
  private val flexmarkAllVersion     = "0.36.8"

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % bootstrapPlay28Version,
    "uk.gov.hmrc" %% "play-hmrc-api"             % playHmrcApiVersion
  )

  val test = Seq(
    "uk.gov.hmrc"          %% "bootstrap-test-play-28" % bootstrapPlay28Version % Test,
    "com.vladsch.flexmark" % "flexmark-all"            % flexmarkAllVersion     % "test, it"
  )
}
