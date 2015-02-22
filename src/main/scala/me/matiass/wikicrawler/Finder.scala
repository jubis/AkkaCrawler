package me.matiass.wikicrawler

import akka.actor._
import akka.util.Timeout
import dispatch.Defaults._
import dispatch._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import java.lang.System.currentTimeMillis
import me.matiass.wikicrawler.Messages._
import me.matiass.wikicrawler.WikiUtils._

class Finder extends Actor with ActorLogging with Stash {

  implicit val timeout = Timeout(10 seconds)

  var collector: ActorRef = null

  findCollector()

  def findCollector() = {
    context.system.actorSelection("user/collector").resolveOne.onComplete{
      case Success(result) => {
        self ! result // access my state only through message queue
      }
      case Failure(e) => log error s"Can't find collector - $e"
    }
  }

  /**
   * This is blocking so that Finder would execute one search at a time.
   * TODO: Remove blocking and implement a queue for links to be handled.
   */
  def loadPage(pageUrl: String) = {
    val future = Http(url(pageUrl) OK as.String)
    try{
      Await.result(future, 10 seconds)
    }
    catch {
      case e: Throwable => {
        log error s"Page load failed in ${self.path}"
        ""
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

      val start = currentTimeMillis

      val hub = context.sender

      val content = loadPage(pageUrl)
      val header = pickHeader(content)

      collector ! Article(header)

      findLinks(content).foreach {
        case url: String => {
          hub ! Link(url)
        }
      }

      val spent = currentTimeMillis - start

      log.info(s"Spent $spent")
    }

  }

}
