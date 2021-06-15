// SBT plugin resolvers
resolvers +=
  Resolver.url("bintray-sbilinski", url("https://dl.bintray.com/sbilinski/maven"))(Resolver.ivyStylePatterns)

// Use git in sbt, show git prompt and use versions from git.
// sbt> git <your git command>
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.1")

// Automatically adds license information to each source code file.
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.6.0")

// Release a new version of the app
// The following command builds a Docker image, publishes it to ECR and bumps the version in version.sbt
// sbt> release
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.0.15")

// Formatting in scala
// See .scalafmt.conf for configuration details.
// Formatting takes place before the project is compiled.
addSbtPlugin( "org.scalameta" % "sbt-scalafmt" % "2.0.7")

// Code coverage report. The code has to be instrumented, therefore a clean build is needed.
// sbt> clean
// sbt> coverage test
// sbt> coverageReport
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.8.2")

// Static code analysis.
// sbt> scapegoat
addSbtPlugin("com.sksamuel.scapegoat" % "sbt-scapegoat" % "1.1.0")
