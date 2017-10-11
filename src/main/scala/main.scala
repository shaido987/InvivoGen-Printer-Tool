package runtime

import logic.html
import scala.xml.Node

object invivogenPDFs {
  def main(args: Array[String]) = {
    val linkFile = "ProductLinks.txt"
    val baseadress = "http://invivogen.cn/"
    val destination = "downloaded TDSs/"

    val lines    = scala.io.Source.fromFile(linkFile).getLines()
    val size     = scala.io.Source.fromFile(linkFile).getLines().size
    for((line,index) <- lines.zipWithIndex) { 
      if (!line.trim.isEmpty) {
        val name :: link :: _ = line.split(",").map(_.trim).toList
        println(s"${index+1}/$size\t- $name\t $link")
        val node: Node = html.loadString(link)
        val pdf        = html.parsePDF(node).head // do not want the MSDS
        val pdfType    = pdf.split("/").head
        html.downloadPDF(baseadress ++ pdf, name ++ "_TDS.pdf", destination)
      }
    }
  }
}