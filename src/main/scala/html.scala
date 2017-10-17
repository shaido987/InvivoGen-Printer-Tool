package logic

import java.net.URL
import java.io.File
import sys.process._

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
  def loadURL(url: URL) = {
    val urlc = url.openConnection()
    load(new sax.InputSource(urlc.getInputStream()))
  }

  def findPDF(node: Node) : Seq[String] = {
    val pdfs = (node \\ "ul").filter(_.attribute("class").getOrElse("").toString == ("liste-pdf")).head
    (pdfs \\ "a").map(_.attribute("href").get.toString)
  }

  def downloadPDF(source: String, name: String , dest: String = "./") : Unit = {
    val file = new File(dest ++ name)
    if (!file.getParentFile.exists)
      file.getParentFile.mkdirs
    new URL(source) #> file !!
  }
}
