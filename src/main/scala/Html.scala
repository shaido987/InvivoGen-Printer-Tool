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
object Html {
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
  def findPDF(node: Node, id: String) : String = {
    val pdfs = (node \\ "ul").filter(_.attribute("class").getOrElse("").toString == ("liste-pdf")).head
    val textLinks: Seq[(String, String)] = (pdfs \\ "a").map(n => (n.text.toString, n.attribute("href").get.toString))
   
    def textContains(text: String, id: String): Boolean = {
      text.toLowerCase.replaceAll("-|_|\\s","").contains(id.toLowerCase.replaceAll("-|_|\\s",""))
    }

    // Check so the id is in the text
    textLinks.filter{case (text, link) => textContains(text, id)}
      .map(_._2)
      .head // Only take the TDS, ignore the MDMS
  }

  /** Downloads a pdf document.
   *
   *  @param source the source adress to download
   *  @param id id of the saved file
   *  @param dest destionation directory
   */
  def downloadPDF(source: String, id: String , dest: String = "./") : Unit = {
    val file = new File(dest ++ id)
    if (!file.getParentFile.exists)
      file.getParentFile.mkdirs
    new URL(source) #> file !!
  }
}
