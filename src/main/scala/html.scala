package logic

import java.net.URL
import java.io.File
import sys.process._

import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl
import org.xml.sax
import org.xml.sax.InputSource

import scala.xml._
import parsing.NoBindingFactoryAdapter

/** Object taking care of all interactions with internet */
object HTML {
  lazy val adapter = new NoBindingFactoryAdapter()
  lazy val parser  = (new SAXFactoryImpl).newSAXParser()
  
  /** Loads a an InputSource as XML */
  def load(source: InputSource) : Node = adapter.loadXML(source, parser)

  /** Loads a String as XML */
  def loadString(source: String) = loadURL(new URL(source))

  /** Loads an URL as XML */
  def loadURL(url: URL) = {
    val urlc = url.openConnection()
    load(new sax.InputSource(urlc.getInputStream()))
  }

  /** Parses an XML Node to find the pdf documents.
   *
   *  @param node the XML node to search
   *  @return all pdf documents meeting the requirements (TDS and MSDS).
   */
  def findPDF(node: Node) : Seq[String] = {
    val pdfs = (node \\ "ul").filter(_.attribute("class").getOrElse("").toString == ("liste-pdf")).head
    (pdfs \\ "a").map(_.attribute("href").get.toString)
  }

  /** Downloads a pdf document.
   *
   *  @param source the source adress to download
   *  @param name name of the saved file
   *  @param dest destionation directory
   */
  def downloadPDF(source: String, name: String , dest: String = "./") : Unit = {
    val file = new File(dest ++ name)
    if (!file.getParentFile.exists)
      file.getParentFile.mkdirs
    new URL(source) #> file !!
  }
}
