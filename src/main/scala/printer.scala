package logic

import java.io.File
import java.awt.print.PrinterJob
import java.awt.print.PrinterException
import javax.print.PrintService
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.printing.PDFPageable

object printer {
  
  lazy val printJob = PrinterJob.getPrinterJob()
  lazy val printer  = if (printJob.printDialog()) Some(printJob.getPrintService()) else None

  def print(pdf: PDDocument, ps: PrintService): Unit = {
    val job = PrinterJob.getPrinterJob()
    job.setPrintService(ps)
    job.setPageable(new PDFPageable(pdf))
    job.print()
  }

  def printPDF(file: File, printer: Option[PrintService] = printer): Unit = {
    if (file.getName().endsWith("pdf")) {
      val pdf = PDDocument.load(file)
      printer match {
        case Some(ps) => print(pdf, ps)
        case None     => throw new PrinterException("No printer found")
      }
    } else {
      throw new PrinterException(file.getName() + " is not a pdf.")
    }
  }

  def printDirectory(dir: String): Unit = {
    val d = new File(dir) 
    for (file <- d.listFiles if file.getName().endsWith(".pdf")) {
      printPDF(file)
    }
  }

  def printOrders(dir: String, orders: Map[String, Int]): Unit = {
    for ((name, num) <- orders) {
      val file = new File(dir + name + ".pdf")
      printPDF(file)
      // TODO: prinitng here, num copies
    }
  }
}
