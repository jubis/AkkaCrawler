package me.matiass.wikicrawler

object Messages {

  case class Article(name: String)

  case class Link(url: String){
    def wikiUrl = s"http://en.wikipedia.org/wiki/$url"
  }

  case class Ready()

}
