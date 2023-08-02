// *****************************************************************************
// Projects
// *****************************************************************************

lazy val itkit =
  project
    .in(file("."))
    .settings(
      name         := "itkit-pekko",
      organization := "io.moia",
      licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
      scmInfo  := Some(ScmInfo(url("https://github.com/moia-oss/itkit-pekko"), "scm:git@github.com:moia-oss/itkit-pekko.git")),
      homepage := Some(url("https://github.com/moia-oss/itkit-pekko"))
    )
    .enablePlugins(
      AutomateHeaderPlugin,
      GitVersioning,
      GitBranchPrompt
    )
    .settings(sonatypeSettings: _*)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings: _*)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        // compile time dependencies
        library.pekkoActor,
        library.pekkoHttp,
        library.pekkoHttpTestkit,
        library.pekkoActorTestkit,
        library.pekkoStream,
        library.log4jApi,
        library.log4jCore,
        library.logJulOverLog4j,
        library.logSlfOverLog4j,
        library.pureConfig,
        library.scalaCheck,
        library.scalaLogging,
        library.scalaTest
      )
    )

lazy val samples =
  project
    .in(file("samples"))
    .configs(IntegrationTest)
    .dependsOn(itkit)
    .settings(Defaults.itSettings: _*)
    .settings(commonSettings)
    .settings(
      fork            := true,
      publishArtifact := false
    )

// *****************************************************************************
// Dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val pekko        = "1.0.1"
      val pekkoHttp    = "1.0.0"
      val log4j        = "2.20.0"
      val pureConfig   = "0.17.4"
      val scalaCheck   = "1.17.0"
      val scalaLogging = "3.9.5"
      val scalaTest    = "3.2.16"
    }
    val pekkoActor        = "org.apache.pekko"           %% "pekko-actor-typed"         % Version.pekko
    val pekkoHttp         = "org.apache.pekko"           %% "pekko-http"                % Version.pekkoHttp
    val pekkoHttpTestkit  = "org.apache.pekko"           %% "pekko-http-testkit"        % Version.pekkoHttp
    val pekkoStream       = "org.apache.pekko"           %% "pekko-stream-typed"        % Version.pekko
    val pekkoActorTestkit = "org.apache.pekko"           %% "pekko-actor-testkit-typed" % Version.pekko
    val log4jApi          = "org.apache.logging.log4j"    % "log4j-api"                 % Version.log4j
    val log4jCore         = "org.apache.logging.log4j"    % "log4j-core"                % Version.log4j
    val logJulOverLog4j   = "org.apache.logging.log4j"    % "log4j-jul"                 % Version.log4j
    val logSlfOverLog4j   = "org.apache.logging.log4j"    % "log4j-slf4j-impl"          % Version.log4j
    val pureConfig        = "com.github.pureconfig"      %% "pureconfig"                % Version.pureConfig
    val scalaCheck        = "org.scalacheck"             %% "scalacheck"                % Version.scalaCheck
    val scalaLogging      = "com.typesafe.scala-logging" %% "scala-logging"             % Version.scalaLogging
    val scalaTest         = "org.scalatest"              %% "scalatest"                 % Version.scalaTest
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val commonSettings =
  compilerSettings ++
    gitSettings ++
    licenseSettings ++
    sbtSettings ++
    scalaFmtSettings ++
    scapegoatSettings ++
    sbtGitSettings

lazy val compilerSettings =
  Seq(
    scalaVersion                                                       := "2.13.11",
    versionScheme                                                      := Some("early-semver"),
    Compile / packageBin / mappings += baseDirectory.value / "LICENSE" -> "LICENSE",
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-release",
      "8",
      "-encoding",
      "UTF-8",
      "-DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector",
      "-Xfatal-warnings",
      "-Xlint:_,-byname-implicit",
      "-Ywarn-numeric-widen",
      "-Ywarn-dead-code",
      "-Ywarn-value-discard",
      "-Xsource:3"
    ),
    javacOptions ++= Seq(
      "-source",
      "1.8",
      "-target",
      "1.8"
    ),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories    := Seq((Test / scalaSource).value),
    resolvers += "Apache Pekko Snapshots".at("https://repository.apache.org/content/groups/snapshots")
  )

lazy val gitSettings = Seq(git.useGitDescribe := true)

lazy val licenseSettings =
  Seq(
    headerLicense := Some(
      HeaderLicense.Custom(
        """|Copyright (c) MOIA GmbH 2017
           |""".stripMargin
      )
    ),
    headerMappings := headerMappings.value + (HeaderFileType.conf -> HeaderCommentStyle.hashLineComment)
  )

lazy val sonatypeSettings = {
  import xerial.sbt.Sonatype._
  Seq(
    publishTo              := sonatypePublishTo.value,
    sonatypeProfileName    := organization.value,
    publishMavenStyle      := true,
    sonatypeProjectHosting := Some(GitHubHosting("moia-oss", "itkit-pekko", "oss-support@moia.io")),
    credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credential")
  )
}

lazy val sbtSettings = Seq(cancelable in Global := true)

lazy val scalaFmtSettings = Seq(scalafmtOnCompile := true)

lazy val scapegoatSettings = Seq(ThisBuild / scapegoatVersion := "2.1.2")

lazy val sbtVersionRegex = "v([0-9]+.[0-9]+.[0-9]+)-?(.*)?".r

lazy val sbtGitSettings = Seq(
  git.useGitDescribe       := true,
  git.baseVersion          := "0.0.0",
  git.uncommittedSignifier := None,
  git.gitTagToVersionNumber := {
    case sbtVersionRegex(v, "")         => Some(v)
    case sbtVersionRegex(v, "SNAPSHOT") => Some(s"$v-SNAPSHOT")
    case sbtVersionRegex(v, s)          => Some(s"$v-$s-SNAPSHOT")
    case _                              => None
  }
)
