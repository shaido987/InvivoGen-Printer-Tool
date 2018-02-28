import java.io.File
import java.awt.print.{PrinterException, PrinterJob}
import javax.print.attribute.HashPrintRequestAttributeSet
import javax.print.attribute.standard.{Chromaticity, Copies, JobName, PageRanges, Sides}

import org.apache.pdfbox.tools.imageio.ImageIOUtil
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.apache.pdfbox.pdmodel.{PDDocument, PDPage, PDPageContentStream}
import org.apache.pdfbox.printing.PDFPageable
import org.apache.pdfbox.rendering.{ImageType, PDFRenderer}

/** Handels all the interactions with the printer */
object Printer {
  
  /** Prints a pdf file to the standard printer with the following settings:
   *  - job name is same as product
   *  - all pages
   *  - double-sided (long edge)
   *  - in color
   *
   *  @param file the pdf file to print in File format
   *  @param numCopies number of printed copies of the file
   */
  def printPDF(file: File, numCopies: Int): Unit = {
    if (!file.getName.endsWith("pdf"))
      throw new PrinterException(file.getName + " is not a pdf")

    val pdf = PDDocument.load(file)
    printPDF(pdf, file.getName, numCopies)
  }

  /** Prints a pdf file to the standard printer with the following settings:
    *  - job name is same as product
    *  - all pages
    *  - double-sided (long edge)
    *  - in color
    *
    *  @param pdf the pdf file to print in PDDocument format
    *  @param name the name of the printjob
    *  @param numCopies number of printed copies of the file
    */
  def printPDF(pdf: PDDocument, name: String, numCopies: Int): Unit = {
    val job = PrinterJob.getPrinterJob
    job.setPageable(new PDFPageable(pdf))

    val attr = new HashPrintRequestAttributeSet()
    attr.add(new PageRanges(1, pdf.getNumberOfPages))
    attr.add(Sides.TWO_SIDED_LONG_EDGE)
    attr.add(new Copies(numCopies))
    attr.add(new JobName(name, null))
    attr.add(Chromaticity.COLOR)

    job.print(attr)

    pdf.close()
  }

  /** Prints a pdf file to the standard printer as an image with the following settings:
   *  - job name is same as product
   *  - all pages
   *  - double-sided (long edge)
   *  - in color
   *
   *  @param file the pdf file to print
   *  @param numCopies number of printed copies of the file
   */
  def printPDFasImage(file: File, numCopies: Int): Unit = {
    if (!file.getName.endsWith("pdf"))
      throw new PrinterException(file.getName + " is not a pdf")
    val pdf = PDDocument.load(file)
    val pdfRenderer = new PDFRenderer(pdf)

    val bims = for (page <- 0 until pdf.getNumberOfPages) yield {
      pdfRenderer.renderImageWithDPI(page, 500, ImageType.RGB)
    }

    val doc = new PDDocument()
    val pageBox = pdf.getPage(0).getCropBox
    for ((bim, index) <- bims.zipWithIndex) {
      //Save image as png test
      //ImageIOUtil.writeImage(bim, file.getName.dropRight(4) + "-" + (index+1) + ".png", 300)

      val page = new PDPage(pageBox)
      doc.addPage(page)
      val pdImageXObject = LosslessFactory.createFromImage(doc, bim)

      // Second bool is compression. Set to false for increased quality
      val contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.OVERWRITE, false)
      contentStream.drawImage(pdImageXObject, 0, 0, pageBox.getWidth, pageBox.getHeight)
      contentStream.close()
    }
    // Save finished pdf test
    //doc.save("test.pdf")

    printPDF(doc, file.getName, numCopies)
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
      printPDFasImage(file, numCopies) //TESTING
    }
  }
}
