package logic

import java.net.URL

import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl
import org.xml.sax
import org.xml.sax.InputSource

import scala.xml._
import parsing.NoBindingFactoryAdapter

object html {
  lazy val adapter = new NoBindingFactoryAdapter()
  lazy val parser  = (new SAXFactoryImpl).newSAXParser()
  
  def load(source: InputSource) : Node = adapter.loadXML(source, parser)
  def loadString(source: String) = loadURL(new URL(source))
  def loadURL(url: URL) = load(new sax.InputSource(url.openConnection().getInputStream))

  def parsePDF(node: Node) : Seq[String] = {
    val pdfs = (node \\ "ul").filter(_.attribute("class").getOrElse("").toString == ("liste-pdf")).head
    (pdfs \\ "a").map(_.attribute("href").get.toString)
  }

  def downloadPDF(source: String, name: String , dest: String = "./") : Unit = {
    import sys.process._
    import java.io.File
    val file = new File(dest ++ name)
    file.getParentFile.mkdirs
    new URL(source) #> file !!
  }
}
