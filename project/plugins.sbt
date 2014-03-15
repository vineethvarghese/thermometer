resolvers += Resolver.url("commbank-releases-ivy", new URL("http://commbank.artifactoryonline.com/commbank/ext-releases-local-ivy"))(Patterns("[organization]/[module]_[scalaVersion]_[sbtVersion]/[revision]/[artifact](-[classifier])-[revision].[ext]"))

addSbtPlugin("au.com.cba.omnia" % "uniform-core" % "0.0.1-20140315104647-0622ea8")

addSbtPlugin("au.com.cba.omnia" % "uniform-dependency" % "0.0.1-20140315104647-0622ea8")
