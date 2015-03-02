package me.matiass.wikicrawler

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.{RoundRobinPool, SmallestMailboxPool}
import me.matiass.wikicrawler.Messages.{Ready, Link}

import scala.collection.immutable.Queue

class Hub extends Actor with ActorLogging {

  var visited = List[Link]() //Using immutable List so that the reference can be shared
  var queue = Queue[Link]()
  
  val collector = context.system.actorOf(Props[Collector], "collector")

  /**
   * This is what makes the difference.
   * Notice that one Finder executes one article at a time so this determines the concurrent amount of
   * articles being loaded and parsed.
   */
  val finderAmount = 10

  val finderRouter = context.actorOf(
    RoundRobinPool(finderAmount).props(Finder.props(self)),
    "finderRouter"
  )

  def receive = {

    case link: Link => {
      if(!visited.contains(link)) {
        queue = queue enqueue link
      }
    }

    case Ready => {
      queue = queue.dequeue match { case (nextLink, remaining) =>
        log.info(s"Dequeue next. Queue length ${queue.length}")
        visited = nextLink :: visited
        finderRouter ! nextLink.wikiUrl
        remaining
      }


    }

  }

}
