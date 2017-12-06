import java.io.{File, FileInputStream, FileNotFoundException}
import java.awt.print.PrinterException
import javax.print.{DocFlavor, PrintService, PrintServiceLookup, SimpleDoc}
import javax.print.attribute.{HashDocAttributeSet, HashPrintRequestAttributeSet}
import javax.print.attribute.standard._

/** Handels all the interactions with the printer */
object Printer {
  
  /** Prints a pdf file to the standard printer with the following settings:
   *  - job name is same as product
   *  - all pages
   *  - double-sided (long edge)
   *  - in color
   *
   *  @param file the pdf file to print
   *  @param numCopies number of printed copies of the file
   */
  def printPDF(file: File, numCopies: Int): Unit = {
    try {
      val flavor = DocFlavor.INPUT_STREAM.AUTOSENSE
      val docAttrs = new HashDocAttributeSet()
      docAttrs.add(new DocumentName(file.getName, null))
      docAttrs.add(Chromaticity.COLOR)
      docAttrs.add(Sides.TWO_SIDED_LONG_EDGE)
      val fis = new FileInputStream(file)
      val doc = new SimpleDoc(fis, flavor, docAttrs)

      val printService = PrintServiceLookup.lookupDefaultPrintService()
      val job = printService.createPrintJob()
      val jobAttrs = new HashPrintRequestAttributeSet()
      jobAttrs.add(new Copies(numCopies))
      jobAttrs.add(new JobName(file.getName, null))

      job.print(doc, jobAttrs)
      fis.close()
    } catch {
      case e: PrinterException => println("Printer exception");  e.printStackTrace()
      case e: FileNotFoundException => println("File not found"); e.printStackTrace()
      case e: NullPointerException => println("No default printer"); e.printStackTrace()
    }
  }

  /** Prints one copy of all pdfs in a directory
   *
   *  @param dir the directory with pdfs to print
   */
  def printDirectory(dir: String): Unit = {
    val d = new File(dir) 
    for (file <- d.listFiles if file.getName.endsWith(".pdf")) {
      printPDF(file, 1)
    }
  }

  /** Prints all orders, requires all TDSs to be downoaded
   *
   *  @param dir directory will downlaoded pdf
   *  @param orders mapping between product name and number of copies to print
   */
  def printOrders(dir: String, orders: Map[String, Int]): Unit = {
    println("-----------------------------")
    println("Starting to print")
    for (((id, numCopies), index) <- orders.zipWithIndex) {
      println(s"${index+1}/${orders.size}\t$id")
      
      val file = new File(dir + id + ".pdf")
      printPDF(file, numCopies)
    }
  }
}
