package runtime

import logic._
import scala.xml.Node
import scala.io.Source


/** Main object for running the TDS download and printing */
object InvivogenTDSPrinter {

  /** Main method, no arguments necessary */
  def main(args: Array[String]) = {
    val linkFile    = "ProductLinks.csv"
    val orderFile   = "Order.csv"
    val baseAdress  = "http://invivogen.com/"
    val destFolder  = "Downloaded TDSs/"
    
    // Print greeting
    printGreeting()

    val orders:  Map[String, Int]    = getOrders(orderFile)
    val linkMap: Map[String, String] = getLinkMap(linkFile)
    val tdsOrders = filterOrdersOnIds(orders, linkMap.keys.toSet)

    downloadOrderTDS(linkMap, tdsOrders.keys.toSeq, baseAdress, destFolder)
    Printer.printOrders(destFolder, tdsOrders)
  }
  
  /** Get all orders from file.
   *
   *  @param orderFile file with orders, shouldn't contain a header.
   *                   Accepted format is "product id", "number of orders".
   *  @return A mapping from product id to number of orders.
   */
  def getOrders(orderFile: String): Map[String, Int] = {
    val orders = Source.fromFile(orderFile).getLines()

    val res = for (order <- orders if order.trim.nonEmpty) yield {
      val id :: numCopies :: _ = order.split(",").map(_.trim.replace("\"", "")).toList
      (id -> numCopies.toInt)
    }
    res.filter(_._2 > 0).toMap  // Only return the orders with 1 or more to-be-printed pdf
  }
  
  /** Get all web adresses for all existing TDSs.
   *
   *  @param linkFile file with web page links, should contain a header.
   *                  Accepted format is "product id", "product webpage"
   *  @return A mapping from product id to web adress.
   */
  def getLinkMap(linkFile: String): Map[String, String] = {
    val idLinks = Source.fromFile(linkFile).getLines().drop(1)

    val res = for (idLink <- idLinks if idLink.trim.nonEmpty) yield {
      val id :: link :: _ = idLink.split(",").map(_.trim).toList
      (id -> link)
    }
    res.toMap
  }

  /** Filter all orders after existig TDS webpages. 
   *  Only orders with a corresponding webpage are printed since not all orders have a TDS.
   *  
   *  @param orders all orders, those with or without a corresponding TDS
   *  @param ids all products that have a TDS
   *  @return all orders with a corresponding TDS
   */
  def filterOrdersOnIds(orders: Map[String, Int], ids: Set[String]): Map[String, Int] = {
    val tdsOrders   = orders filterKeys ids
    val otherOrders = orders -- tdsOrders.keys.toSet
   
    // Printing information about the orders to user
    println(s"Orders in TDS link list, total: ${tdsOrders.size}")
    tdsOrders.keys foreach println
    println(s"\nOrders not in TDS list, total: ${otherOrders.size}")
    otherOrders.keys foreach println
    println("-----------------------------")

    tdsOrders 
  }

  /** Downloads a single TDS.
   *
   *  @param id the id of the TDS, will become the pdf name.
   *  @param link link to the product page of the order
   *  @param baseAdress the base adress to invivogen
   *  @param destFolder folder to save the pdf
   */
  def downloadTDS(id: String, link: String, baseAdress: String, destFolder: String): Unit = {
    val cleanId = id.replaceAll("-|_","")
    val node: Node = HTML.loadString(link)

    // multiple TDS documents can be on the same webpage and do not want the MSDS
    val tds = HTML.findPDF(node).filter(f => f.toLowerCase.replaceAll("-|_","").contains(cleanId)).head
    HTML.downloadPDF(baseAdress + tds, id + ".pdf", destFolder)
  }

  /** Downloads all TDS for all orders with one.
   *
   *  @param linkMap mapping from product id to product webpage
   *  @param ids the ids of all products to be downloaded
   *  @param baseAdress the base adress to invivogen
   *  @param destFolder folder to save all pdfs
   */
  def downloadOrderTDS(linkMap: Map[String, String], ids: Seq[String], baseAdress: String, destFolder: String): Unit = {
    println(s"Downloading all TDS documents, total: ${ids.length}")
    for ((id, index) <- ids.zipWithIndex) {
      println(s"${index+1}/${ids.length}\t$id")

      linkMap.get(id) match {
        case Some(link) => downloadTDS(id, link, baseAdress, destFolder)
        case None       => throw new NoSuchElementException("The id " + id + " not in link csv file.")
      }
    }
  }

  /** Startup greeting to the user */
  def printGreeting(): Unit = {
    val greeting = 
      """
      |-----------------------------
      ||                           |
      ||   Invivogen TDS Printer   |
      ||                           |
      |-----------------------------
      || version: 1.0.2            |
      || author : shaido987        |
      || source : @github.com      |
      |-----------------------------
      """

    println(greeting.stripMargin)
  }
}
