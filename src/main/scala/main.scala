package runtime

import logic.html
import logic.printer
import scala.xml.Node
import scala.io.Source

object InvivogenTDSPrinter {

  def main(args: Array[String]) = {
    val linkFile    = "ProductLinks.csv"
    val orderFile   = "Order.csv"
    val baseAdress  = "http://invivogen.com/"
    val destFolder  = "Downloaded TDSs/"
    
    val orders:  Map[String, Int]    = getOrders(orderFile)
    val linkMap: Map[String, String] = getLinkMap(linkFile)

    // Filter orders after names in linkMap
    // Needed as not all orders are to-be-printed TDSs
    val tdsOrders   = orders filterKeys linkMap.keys.toSet
    val otherOrders = orders filterKeys tdsOrders.keys.toSet

    // Printing information about the orders
    println("-----------------------")
    println(s"Orders in TDS link list, total: ${tdsOrders.size}")
    tdsOrders.keys foreach println
    println(s"\nOrders not in TDS list, total: ${otherOrders.size}")
    otherOrders.keys foreach println
    println("-----------------------")

    downloadOrderTDS(linkMap, tdsOrders.keys.toSeq, baseAdress, destFolder)
    printer.printOrders(destFolder, tdsOrders)
  }

  def getOrders(orderFile: String): Map[String, Int] = {
    val orders = Source.fromFile(orderFile).getLines().drop(1)

    val res = for (order <- orders if order.trim.nonEmpty) yield {
      val name :: numCopies :: _ = order.split(",").map(_.trim.replace("\"", "")).toList
      (name -> numCopies.toInt)
    }
    res.filter(_._2 > 0).toMap  // Only return the orders with 1 or more to-be-printed pdf
  }
  
  def getLinkMap(linkFile: String): Map[String, String] = {
    val nameLinks = Source.fromFile(linkFile).getLines().drop(1)

    val res = for (nameLink <- nameLinks if nameLink.trim.nonEmpty) yield {
      val name :: link :: _ = nameLink.split(",").map(_.trim).toList
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
      println(s"${index+1}/${names.length}\t$name")

      linkMap.get(name) match {
        case Some(link) => downloadTDS(name, link, baseAdress, destFolder)
        case None       => throw new NoSuchElementException("The name " + name + " not in link csv file.")
      }
    }
  }

  def downloadAllTDS(linkMap: Map[String, String], baseAdress: String, destFolder: String): Unit = {
    for(((name, link), index) <- linkMap.zipWithIndex) { 
      println(s"${index+1}/${linkMap.size}\t- $name\t $link")
      downloadTDS(name, link, baseAdress, destFolder)
    }
  }
}
