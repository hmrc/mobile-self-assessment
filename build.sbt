import play.sbt.PlayImport.PlayKeys.playDefaultPort
import sbt.Tests.{Group, SubProcess}

val appName = "mobile-self-assessment"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(
    Seq(
      play.sbt.PlayScala,
      SbtAutoBuildPlugin,
      SbtDistributablesPlugin,
      ScoverageSbtPlugin
    ): _*
  )
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    routesImport ++= Seq(
      "uk.gov.hmrc.mobileselfassessment.config.binders.Binders._",
      "uk.gov.hmrc.mobileselfassessment.model.types._",
      "uk.gov.hmrc.mobileselfassessment.model.types.ModelTypes._"
    )
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(
    majorVersion := 0,
    scalaVersion := "2.13.8",
    playDefaultPort := 8261,
    libraryDependencies ++= AppDependencies(),
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    unmanagedResourceDirectories in Compile += baseDirectory.value / "resources",
    unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest)(base =>
      Seq(base / "it", base / "test-common")
    ).value,
    unmanagedSourceDirectories in Test := (baseDirectory in Test)(base => Seq(base / "test", base / "test-common")).value,
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-language:higherKinds",
      "-language:postfixOps",
      "-feature",
      "-Ywarn-dead-code",
      "-Ywarn-value-discard",
      "-Ywarn-numeric-widen",
      "-Xlint"
    ),
    coverageMinimumStmtTotal := 80,
    coverageFailOnMinimum := true,
    coverageHighlighting := true,
    coverageExcludedPackages := "<empty>;com.kenshoo.play.metrics.*;.*definition.*;prod.*;testOnlyDoNotUseInAppConf.*;app.*;.*BuildInfo.*;.*Routes.*;.*javascript.*;.*Reverse.*;.*Hooks.*;.*DescriptionCode;.*TaxToPayStatus"
    // ***************
  )

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] =
  tests map { test =>
    Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }

