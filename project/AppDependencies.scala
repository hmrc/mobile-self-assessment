import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  private val bootstrapPlay28Version = "5.24.0"
  private val playHmrcApiVersion     = "7.0.0-play-28"
  private val flexmarkAllVersion     = "0.36.8"
  private val jsonJodaVersion        = "2.9.2"
  private val domainVersion          = "8.1.0-play-28"
  private val refinedVersion         = "0.9.26"
  private val taxYearVersion         = "3.0.0"

  private val pegdownVersion       = "1.6.0"
  private val wireMockVersion      = "2.20.0"
  private val scalaTestVersion     = "3.2.9"
  private val scalaTestPlusVersion = "5.1.0"
  private val scalaMockVersion     = "5.1.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28" % bootstrapPlay28Version,
    "uk.gov.hmrc"       %% "play-hmrc-api"             % playHmrcApiVersion,
    "com.typesafe.play" %% "play-json-joda"            % jsonJodaVersion,
    "uk.gov.hmrc"       %% "domain"                    % domainVersion,
    "eu.timepit"        %% "refined"                   % refinedVersion,
    "uk.gov.hmrc"       %% "tax-year"                  % taxYearVersion
  )

  val test = Seq(
    Test,
    "com.vladsch.flexmark" % "flexmark-all" % flexmarkAllVersion % "test, it"
  )

  trait TestDependencies {
    lazy val scope: String        = "test"
    lazy val test:  Seq[ModuleID] = ???
  }

  object Test {

    def apply(): Seq[ModuleID] =
      new TestDependencies {

        override lazy val test: Seq[ModuleID] = testCommon(scope) ++ Seq(
            "uk.gov.hmrc"   %% "bootstrap-test-play-28" % bootstrapPlay28Version,
            "org.scalamock" %% "scalamock"              % scalaMockVersion % scope
          )
      }.test
  }

  object IntegrationTest {

    def apply(): Seq[ModuleID] =
      new TestDependencies {

        override lazy val scope: String = "it"

        override lazy val test: Seq[ModuleID] = testCommon(scope) ++ Seq(
            "com.github.tomakehurst" % "wiremock" % wireMockVersion % scope
          )
      }.test
  }

  private def testCommon(scope: String) = Seq(
    "org.pegdown"            % "pegdown"             % pegdownVersion % scope,
    "com.typesafe.play"      %% "play-test"          % PlayVersion.current % scope,
    "org.scalatest"          %% "scalatest"          % scalaTestVersion,
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % scope,
    "com.vladsch.flexmark"   % "flexmark-all"        % flexmarkAllVersion % scope
  )

  def apply(): Seq[ModuleID] = compile ++ Test() ++ IntegrationTest()
}
