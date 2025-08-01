import play.sbt.PlayImport.PlayKeys.playDefaultPort

val appName = "mobile-self-assessment"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(
    Seq(
      play.sbt.PlayScala,
      SbtDistributablesPlugin,
      ScoverageSbtPlugin
    ): _*
  )
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    routesImport ++= Seq(
      "uk.gov.hmrc.mobileselfassessment.config.binders.Binders._",
      "uk.gov.hmrc.mobileselfassessment.model.types._",
      "uk.gov.hmrc.mobileselfassessment.model.types.JourneyId._"
    )
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    majorVersion := 0,
    scalaVersion := "3.6.4",
    playDefaultPort := 8261,
    libraryDependencies ++= AppDependencies(),
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory)(base => Seq(base / "it", base / "test-common")).value,
    Test / unmanagedSourceDirectories := (Test / baseDirectory)(base => Seq(base / "test", base / "test-common")).value,
    IntegrationTest / parallelExecution := false,
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
