import play.Project._

name := "FEO-DevTool"

version := "1.0"

resolvers += "phloc.com" at "http://repo.phloc.com/maven2"

libraryDependencies ++= Seq(
  "org.jsoup"%"jsoup"%"1.7.3",
  "com.yahoo.platform.yui"%"yuicompressor"%"2.4.7",
  "com.phloc"%"phloc-css"%"3.7.4",
  "org.seleniumhq.selenium" % "selenium-chrome-driver" % "2.41.0",
  "org.seleniumhq.selenium" % "selenium-java" % "2.41.0",
  "com.typesafe.slick" %% "slick" % "2.0.1",
  "com.typesafe.play" % "play-slick_2.10" % "0.6.0.1",
  "mysql" % "mysql-connector-java" % "5.1.18",
  "io.netty"%"netty-all"%"4.0.19.Final",
  "commons-net"%"commons-net"%"3.3"
)

playScalaSettings