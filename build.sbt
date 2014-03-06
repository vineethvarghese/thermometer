uniform.project("thermometer", "au.com.cba.omnia.thermometer")

uniformDependencySettings

libraryDependencies ++=
  depend.scalding() ++ depend.hadoop() ++ depend.scalaz() ++ Seq(
    "org.specs2"              %% "specs2"                      % depend.versions.specs,
    "org.scalacheck"          %% "scalacheck"                  % depend.versions.scalacheck,
    "org.scalaz"              %% "scalaz-scalacheck-binding"   % depend.versions.scalaz
 )
