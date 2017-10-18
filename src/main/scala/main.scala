package runtime

import logic.html
import logic.printer
import scala.xml.Node
import scala.io.Source

object invivogenTDSPrinter {

  def main(args: Array[String]) = {
    val linkFile    = "ProductLinks.csv"
    val orderFile   = "Order.csv"
    val baseAdress  = "http://invivogen.com/"
    val destFolder  = "downloaded TDSs/"
    
    val orders  = getOrders(orderFile)
    val linkMap = getLinkMap(linkFile)

    //downloadAllTDS(linkMap, baseAdress, destFolder)
    downloadOrderTDS(linkMap, orders.map(_._1).toSeq, baseAdress, destFolder)
    //printer.printOrders(destFolder, orders)
  }

  def getOrders(orderFile: String): Map[String, Int] = {
    val orders = Source.fromFile(orderFile).getLines().drop(1)

    val res = for (order <- orders if order.trim.nonEmpty) yield {
      val name :: num :: _ = order.split(",").map(_.trim()).toList
      (name -> num.toInt)
    }
    res.toMap
  }
  
  def getLinkMap(linkFile: String): Map[String, String] = {
    val nameLinks = Source.fromFile(linkFile).getLines().drop(1)

    val res = for (nameLink <- nameLinks if nameLink.trim.nonEmpty) yield {
      val name :: link :: _ = nameLink.split(",").map(_.trim()).toList
      (name -> link)
    }
    res.toMap
  }

  def downloadTDS(name: String, link: String, baseAdress: String, destFolder: String): Unit = {
    val node: Node = html.loadString(link)
    val tds        = html.findPDF(node).head // do not want the MSDS
    html.downloadPDF(baseAdress + tds, name + ".pdf", destFolder)
  }

  def downloadOrderTDS(linkMap: Map[String, String], names: Seq[String], baseAdress: String, destFolder: String): Unit = {
    for ((name, index) <- names.zipWithIndex) {
      val link = linkMap.get(name) match {
        case Some(link) => link
        case None       => { throw new NoSuchElementException("The name " + name + " not in link csv file."); ""}
      }
      println(s"${index+1}/${names.length}\t$name\t$link")
      downloadTDS(name, link, baseAdress, destFolder)
    }
  }

  def downloadAllTDS(linkMap: Map[String, String], baseAdress: String, destFolder: String): Unit = {
    for(((name, link), index) <- linkMap.zipWithIndex) { 
      println(s"${index+1}/${linkMap.size}\t- $name\t $link")
      downloadTDS(name, link, baseAdress, destFolder)
    }
  }
}
