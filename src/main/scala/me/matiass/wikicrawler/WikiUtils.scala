package me.matiass.wikicrawler

object WikiUtils {

  val href = """href="/wiki/([^:\s]*)"""".r.unanchored
  val header = """<h1 id="firstHeading" class="firstHeading" lang="en">(.+)<\/h1>""".r.unanchored

  def findLinks(content: String) = href findAllMatchIn content map(_ group 1)
  def pickHeader(content: String) = content match {
    case header(value) => value replaceAll ("<\\/?i>", "")
    case _ => "unknown"
  }

  def constant(value: String) = Stream.continually(value).toIterator

}
