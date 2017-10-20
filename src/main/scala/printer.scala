package logic

import java.io.File
import java.awt.print.PrinterJob
import java.awt.print.PrinterException

import javax.print.PrintService
import javax.print.attribute.HashPrintRequestAttributeSet
import javax.print.attribute.PrintRequestAttributeSet
import javax.print.attribute.standard.PageRanges
import javax.print.attribute.standard.Sides
import javax.print.attribute.standard.Copies

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.printing.PDFPageable


object Printer {
  
  //lazy val printJob = PrinterJob.getPrinterJob()
  //lazy val printer  = if (printJob.printDialog()) Some(printJob.getPrintService()) else None

  private def print(pdf: PDDocument, numCopies: Int): Unit = {
    val job = PrinterJob.getPrinterJob()
    //job.setPrintService(ps)
    job.setPageable(new PDFPageable(pdf))

    val attr = new HashPrintRequestAttributeSet()
    attr.add(new PageRanges(1, 2)) // pages 1 and 2
    attr.add(Sides.TWO_SIDED_LONG_EDGE)
    attr.add(new Copies(numCopies))
    job.print(attr) //silent print ?
  }

  def printPDF(file: File, numCopies: Int): Unit = { //, printer: Option[PrintService] = printer): Unit = {
    if (file.getName().endsWith("pdf")) {
      val pdf = PDDocument.load(file)
      print(pdf, numCopies)
//      printer match {
//        case Some(ps) => print(pdf, ps)
//        case None     => throw new PrinterException("No printer found")
//      }
    } else {
      throw new PrinterException(file.getName() + " is not a pdf.")
    }
  }

  def printDirectory(dir: String): Unit = {
    val d = new File(dir) 
    for (file <- d.listFiles if file.getName().endsWith(".pdf")) {
      printPDF(file, 1)
    }
  }

  def printOrders(dir: String, orders: Map[String, Int]): Unit = {
    //for ((name, num) <- orders) {
    val (name, numCopies) = orders.head
    val file = new File(dir + name + ".pdf")
    //printPDF(file, numCopies)
    printPDF(file, 1)
      // TODO: prinitng here, num copies
   // }
  }
}
