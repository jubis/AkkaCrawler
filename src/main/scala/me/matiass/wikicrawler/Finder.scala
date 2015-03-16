package me.matiass.wikicrawler

import java.lang.System.currentTimeMillis

import akka.actor._
import akka.util.Timeout
import dispatch.Defaults._
import dispatch._
import me.matiass.wikicrawler.Finder.{Content, FoundPage, LoadPage}
import me.matiass.wikicrawler.Messages._
import me.matiass.wikicrawler.WikiUtils._

import scala.collection.immutable.Queue
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Finder {
  case class LoadPage(pageUrl: String)
  case class FoundPage(content: Content, start: Long)
  case class Content(value: String)

  def props(hub: ActorRef) = Props(new Finder(hub))
}

class Finder(hub: ActorRef) extends Actor with ActorLogging with Stash {

  implicit val timeout = Timeout(10 seconds)

  var collector: ActorRef = null
  var queue = Queue[String]()

  findCollector()

  def findCollector() = {
    context.system.actorSelection("user/collector").resolveOne.onComplete{
      case Success(result) => {
        self ! result // access my state only through message queue
      }
      case Failure(e) => log error s"Can't find collector - $e"
    }
  }

  def loadPage(pageUrl: String): Future[String] = {

    val contentReq = Http(url(pageUrl) OK as.String).either

    contentReq.map {
      case Right(content) => content
      case Left(err) => {
        log.error(s"Page load failed ($pageUrl)")
        ""
      }
    }

  }

  /**
   * Removes first (=current) url from the queue and returns the next one.
   * Return left if queue is empty
   */
  def dequeue(): Option[String] = {
    queue.dequeue match {
      case (prevUrl, remainingQueue) => {
        queue = remainingQueue
        log.info(s"Dequeue $prevUrl. Queue lenght ${queue.length}")

        if(queue.length > 0) Some(queue.front)
        else None
      }
    }
  }

  /**
   * Initial Receive to wait until Collector has been found
   * Stashes all messages to be handled when Collector is ready.
   */
  def receive = {

    case foundCollector: ActorRef => {
      log info "Got the collector - lets go!"
      collector = foundCollector
      context.become(fullRecieve, true)
      hub ! Ready
      unstashAll()
    }

    case _ => {
      log.info("Stashing message")
      stash()
    }
  }

  /**
   * Actual Receive for the actor to become when it's ready to go
   */
  val fullRecieve: Receive = {

    case pageUrl: String => {
      if(queue.length > 0) {
        log.info(s"To queue $pageUrl. Queue lenght ${queue.length}")
      }
      else {
        log.info("No queue")
        self ! LoadPage(pageUrl)
      }
      queue = queue enqueue pageUrl
    }

    case LoadPage(pageUrl) => {

      val start = currentTimeMillis

      for(
        content <- loadPage(pageUrl)
      ) self ! FoundPage(Content(content), start)

    }

    case FoundPage(content, start) => {
      for(
        next <- dequeue()
      ) self ! LoadPage(next)

      val header = pickHeader(content.value)

      val spent = currentTimeMillis - start

      collector ! Article(header)

      for(
        url <- findLinks(content.value)
      ) hub ! Link(url)

      hub ! Ready

      log.info(s"Spent $spent")
    }

  }

}
