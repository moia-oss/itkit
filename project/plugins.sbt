// SBT plugin resolvers
resolvers +=
  Resolver.url("bintray-sbilinski", url("https://dl.bintray.com/sbilinski/maven"))(Resolver.ivyStylePatterns)

// Use git in sbt, show git prompt and use versions from git.
// sbt> git <your git command>
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.2")

// Automatically adds license information to each source code file.
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.6.0")

// Formatting in scala
// See .scalafmt.conf for configuration details.
// Formatting takes place before the project is compiled.
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")

// Static code analysis.
// sbt> scapegoat
addSbtPlugin("com.sksamuel.scapegoat" % "sbt-scapegoat" % "1.1.1")

// Publish to sonatype
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.12")

// publishSigned
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.1.2")

// sbt> mimaReportBinaryIssues
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.0.1")
