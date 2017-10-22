name := "InvivogenTDSPrinter"
version := "1.0.1"

scalaVersion := "2.11.6"

mainClass in (Compile, run) := Some("runtime.InvivogenTDSPrinter")

libraryDependencies += "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5"
libraryDependencies += "org.ccil.cowan.tagsoup" % "tagsoup" % "1.2.1"
libraryDependencies += "org.apache.pdfbox" % "pdfbox" % "2.0.7"
