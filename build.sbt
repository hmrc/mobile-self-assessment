import play.sbt.PlayImport.PlayKeys.playDefaultPort
import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

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
  .settings(publishingSettings: _*)
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
    scalaVersion := "2.12.8",
    playDefaultPort := 8247,
    libraryDependencies ++= AppDependencies(),
    dependencyOverrides ++= overrides,
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
      "-Ypartial-unification",
      "-Ywarn-dead-code",
      "-Ywarn-value-discard",
      "-Ywarn-inaccessible",
      "-Ywarn-infer-any",
      "-Ywarn-nullary-override",
      "-Ywarn-nullary-unit",
      "-Ywarn-numeric-widen",
      //"-Ywarn-unused-import", - does not work well with fatal-warnings because of play-generated sources
      //"-Xfatal-warnings",
      "-Xlint"
    ),
    coverageMinimumStmtTotal := 80,
    coverageFailOnMinimum := true,
    coverageHighlighting := true,
    coverageExcludedPackages := "<empty>;com.kenshoo.play.metrics.*;.*definition.*;prod.*;testOnlyDoNotUseInAppConf.*;app.*;.*BuildInfo.*;.*Routes.*;.*javascript.*;.*Reverse.*;.*Hooks.*"
    // ***************
  )

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] =
  tests map { test =>
    Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }

// Transitive dependencies in scalatest/scalatestplusplay drag in a newer version of jetty that is not
// compatible with wiremock, so we need to pin the jetty stuff to the older version.
// see https://groups.google.com/forum/#!topic/play-framework/HAIM1ukUCnI
val jettyVersion = "9.2.13.v20150730"

val overrides: Seq[ModuleID] = Seq(
  "org.eclipse.jetty"           % "jetty-server"       % jettyVersion,
  "org.eclipse.jetty"           % "jetty-servlet"      % jettyVersion,
  "org.eclipse.jetty"           % "jetty-security"     % jettyVersion,
  "org.eclipse.jetty"           % "jetty-servlets"     % jettyVersion,
  "org.eclipse.jetty"           % "jetty-continuation" % jettyVersion,
  "org.eclipse.jetty"           % "jetty-webapp"       % jettyVersion,
  "org.eclipse.jetty"           % "jetty-xml"          % jettyVersion,
  "org.eclipse.jetty"           % "jetty-client"       % jettyVersion,
  "org.eclipse.jetty"           % "jetty-http"         % jettyVersion,
  "org.eclipse.jetty"           % "jetty-io"           % jettyVersion,
  "org.eclipse.jetty"           % "jetty-util"         % jettyVersion,
  "org.eclipse.jetty.websocket" % "websocket-api"      % jettyVersion,
  "org.eclipse.jetty.websocket" % "websocket-common"   % jettyVersion,
  "org.eclipse.jetty.websocket" % "websocket-client"   % jettyVersion
)
