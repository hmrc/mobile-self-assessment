import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  private val bootstrapPlay28Version = "9.7.0"
  private val playHmrcApiVersion     = "8.0.0"
  private val jsonJodaVersion        = "2.10.6"
  private val domainVersion          = "10.0.0"
  private val refinedVersion         = "0.11.2"
  private val taxYearVersion         = "5.0.0"

  private val scalaMockVersion = "5.2.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapPlay28Version,
    "uk.gov.hmrc"       %% "play-hmrc-api-play-30"     % playHmrcApiVersion,
    "com.typesafe.play" %% "play-json-joda"            % jsonJodaVersion,
    "uk.gov.hmrc"       %% "domain-play-30"            % domainVersion,
    "eu.timepit"        %% "refined"                   % refinedVersion,
    "uk.gov.hmrc"       %% "tax-year"                  % taxYearVersion
  )

  trait TestDependencies {
    lazy val scope: String        = "test"
    lazy val test:  Seq[ModuleID] = ???
  }

  object Test {

    def apply(): Seq[ModuleID] =
      new TestDependencies {

        override lazy val test: Seq[ModuleID] = testCommon(scope) ++ Seq(
            "org.scalamock" %% "scalamock" % scalaMockVersion % scope
          )
      }.test
  }

  object IntegrationTest {

    def apply(): Seq[ModuleID] =
      new TestDependencies {

        override lazy val scope: String = "it"

        override lazy val test: Seq[ModuleID] = testCommon(scope) ++ Seq.empty
      }.test
  }

  private def testCommon(scope: String) = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapPlay28Version % scope
  )

  def apply(): Seq[ModuleID] = compile ++ Test() ++ IntegrationTest()
}
