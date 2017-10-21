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
import javax.print.attribute.standard.JobName

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.printing.PDFPageable


object Printer {
  
  def printPDF(file: File, numCopies: Int): Unit = {
    if (!file.getName().endsWith("pdf")) 
      throw new PrinterException(file.getName() + " is not a pdf")

    val pdf = PDDocument.load(file)
    val job = PrinterJob.getPrinterJob()
    job.setPageable(new PDFPageable(pdf))

    val attr = new HashPrintRequestAttributeSet()
    attr.add(new PageRanges(1, pdf.getNumberOfPages())) // pages 1 and 2
    attr.add(Sides.TWO_SIDED_LONG_EDGE)
    attr.add(new Copies(numCopies))
    attr.add(new JobName(file.getName(), null))
    job.print(attr)
  }

  def printDirectory(dir: String): Unit = {
    val d = new File(dir) 
    for (file <- d.listFiles if file.getName().endsWith(".pdf")) {
      printPDF(file, 1)
    }
  }

  def printOrders(dir: String, orders: Map[String, Int]): Unit = {
    for ((name, num) <- orders) {
      val (name, numCopies) = orders.head
      val file = new File(dir + name + ".pdf")
      printPDF(file, numCopies)
    }
  }
}
