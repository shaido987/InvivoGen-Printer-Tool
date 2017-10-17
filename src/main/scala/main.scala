package runtime

import logic.html
import logic.printer
import scala.xml.Node
import scala.io.Source

object invivogenTDSPrinter {

  def main(args: Array[String]) = {
    val linkFile    = "ProductLinks.txt"
    val baseAdress  = "http://invivogen.com/"
    val destFolder  = "downloaded TDSs/"
    
    //downloadAllTDS(linkFile, baseAdress, destFolder)
    printer.printDirectory(destFolder)
  }


  def downloadAllTDS(linkFile: String, baseAdress: String, destFolder: String) {
    val lines = Source.fromFile(linkFile).getLines().toSeq
    
    for((line, index) <- lines.zipWithIndex) { 
      if (line.trim.nonEmpty) {
        val name :: link :: _ = line.split(",").map(_.trim).toList
        println(s"${index+1}/${lines.size}\t- $name\t $link")

        val node: Node = html.loadString(link)
        val tds        = html.findPDF(node).head // do not want the MSDS
        html.downloadPDF(baseAdress ++ tds, name ++ "_TDS.pdf", destFolder)
      }
    }
  }
}
