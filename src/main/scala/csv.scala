package logic

object CSV {

  def read(source: String) : Array[String] = {
    val bufferedSource = io.Source.fromFile(source)
    val names = bufferedSource.getLines.toArray.map(_.split(",")(2).trim)
    bufferedSource.close
    parse(names.tail)
  }
  
  def parse(names: Array[String]) : Array[String] = {
    val ns  = names.map(_.toLowerCase.replace('/', '_'))
    ns.map(removeCells)
      .map(changeSpecialChars)
      .map(_.replace(' ', '_').replace('-', '_').replace("__", "_"))
      .map(specialCases)
  }
  
  def removeCells(name: String) : String = {
    name.replace(" cells", "") 
  }
  
  //alpha, beta, gamma, trademark
  def changeSpecialChars(name: String) : String = {
    val ns = Seq("α" -> "a", "β" -> "b", "γ" -> "g").foldLeft(name){case (z, (s,r)) => z.replaceAll(s, r)}
    ns.replaceAll("[^\\p{ASCII}]", ""); //last, to remove tm
  }

  def specialCases(name: String) : String = name match {
    case _ => name
  }
}

