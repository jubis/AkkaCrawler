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

  /**
   * Tries to find the Collector and tells it to self.
   */
  def findCollector() = {
    context.system.actorSelection("user/collector").resolveOne.onComplete{
      case Success(result) => {
        self ! result // access my state only through message queue
      }
      case Failure(e) => log error s"Can't find collector - $e"
    }
  }

  /**
   * This is blocking so that Finder can execute one search at a time.
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
   * Stashes all messages for later handling.
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
      //Thread.sleep(1000) //Hack to prevent the SmallestMailboxRouter sending all messages to this
    }
  }

  /**
   * Actual Receive for the actor to become when it's ready to go
   */
  val fullRecieve: Receive = {

    case Link(pageUrl) => {

      val start = currentTimeMillis

      val hub = context.sender

      val content = loadPage(pageUrl)
      val header = pickHeader(content)

      collector ! Article(header)

      findLinks(content).zip(constant(header)).foreach {
        case (link, header) => {
          hub ! Link(link)
        }
      }

      val spent = currentTimeMillis - start

      log.info(s"Spent $spent")
    }

  }

}
