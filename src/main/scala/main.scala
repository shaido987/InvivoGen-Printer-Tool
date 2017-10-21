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
    greeting()

    val orders:  Map[String, Int]    = getOrders(orderFile)
    val linkMap: Map[String, String] = getLinkMap(linkFile)
    val tdsOrders = filterOrdersOnNames(orders, linkMap.keys.toSet)

    downloadOrderTDS(linkMap, tdsOrders.keys.toSeq, baseAdress, destFolder)
    Printer.printOrders(destFolder, tdsOrders)
  }
  
  /** Get all orders from file.
   *
   *  @param orderFile file with orders, shouldn't contain a header.
   *                   Accepted format is "TDS name", "number of orders".
   *  @return A mapping from TDS name to number of orders.
   */
  def getOrders(orderFile: String): Map[String, Int] = {
    val orders = Source.fromFile(orderFile).getLines()

    val res = for (order <- orders if order.trim.nonEmpty) yield {
      val name :: numCopies :: _ = order.split(",").map(_.trim.replace("\"", "")).toList
      (name -> numCopies.toInt)
    }
    res.filter(_._2 > 0).toMap  // Only return the orders with 1 or more to-be-printed pdf
  }
  
  /** Get all web adresses for all existing TDSs.
   *
   *  @param linkFile file with web page links, should contain a header.
   *                  Accepted format is "TDS name", "product webpage"
   *  @return A mapping from TDS name to web adress.
   */
  def getLinkMap(linkFile: String): Map[String, String] = {
    val nameLinks = Source.fromFile(linkFile).getLines().drop(1)

    val res = for (nameLink <- nameLinks if nameLink.trim.nonEmpty) yield {
      val name :: link :: _ = nameLink.split(",").map(_.trim).toList
      (name -> link)
    }
    res.toMap
  }

  /** Filter all orders after existig TDS webpages. 
   *  Only orders with a corresponding webpage are printed since not all orders have a TDS.
   *  
   *  @param orders all orders, those with or without a corresponding TDS
   *  @param names all products that have a TDS
   *  @return all orders with a corresponding TDS
   */
  def filterOrdersOnNames(orders: Map[String, Int], names: Set[String]): Map[String, Int] = {
    val tdsOrders   = orders filterKeys names
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
   *  @param name name of the TDS, will become the pdf name.
   *  @param link link to the product page of the order
   *  @param baseAdress the base adress to invivogen
   *  @param destFolder folder to save the pdf
   */
  def downloadTDS(name: String, link: String, baseAdress: String, destFolder: String): Unit = {
    val node: Node = HTML.loadString(link)
    val tds        = HTML.findPDF(node).head // do not want the MSDS
    HTML.downloadPDF(baseAdress + tds, name + ".pdf", destFolder)
  }

  /** Downloads all TDS for all orders with one.
   *
   *  @param linkMap mapping from name to product webpage
   *  @param names the names of all products to be downloaded
   *  @param baseAdress the base adress to invivogen
   *  @param destFolder folder to save all pdfs
   */
  def downloadOrderTDS(linkMap: Map[String, String], names: Seq[String], baseAdress: String, destFolder: String): Unit = {
    for ((name, index) <- names.zipWithIndex) {
      println(s"${index+1}/${names.length}\t$name")

      linkMap.get(name) match {
        case Some(link) => downloadTDS(name, link, baseAdress, destFolder)
        case None       => throw new NoSuchElementException("The name " + name + " not in link csv file.")
      }
    }
  }

  /** Startup greeting to the user */
  def greeting(): Unit = {
    val greeting = 
      """
      |-----------------------------
      ||                           |
      ||   Invivogen TDS Printer   |
      ||                           |
      |-----------------------------
      || version: 1.0              |
      || author : shaido987        |
      || source : @github          |
      |-----------------------------
      """

    println(greeting.stripMargin)
  }
}
