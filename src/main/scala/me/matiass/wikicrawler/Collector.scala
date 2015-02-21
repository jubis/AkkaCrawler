package me.matiass.wikicrawler

import java.lang.System._

import akka.actor.{ActorLogging, Actor}
import me.matiass.wikicrawler.Messages.Article

class Collector extends Actor with ActorLogging {

  val startTime = currentTimeMillis
  var found = List[Article]()

  def receive = {
    case article: Article => {
      if(found.contains(article)) {
        log.info(s"Received old article")
      }
      else {
        log.info(s"Found article ${article.name} by ${sender.path}")
        found = article :: found

        val velocity = (currentTimeMillis - startTime) / 1000f / found.size
        log.info(s"Avg velocity $velocity")
      }
    }
  }

}
