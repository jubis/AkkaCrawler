package me.matiass.wikicrawler

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.SmallestMailboxPool
import me.matiass.wikicrawler.Messages.Link

class Hub extends Actor with ActorLogging {

  var visited = List[Link]() //Using immutable List so that the reference can be shared

  val collector = context.system.actorOf(Props[Collector], "collector")

  /**
   * This is what makes the difference.
   * Notice that one Finder executes one article at a time so this determines the concurrent amount of
   * articles being loaded and parsed.
   */
  val finderAmount = 10

  val finderRouter = context.actorOf(
    SmallestMailboxPool(finderAmount).props(Props[Finder]),
    "finderRouter"
  )

  def receive = {

    case link: Link => {
      if(!visited.contains(link)) {
        visited = link :: visited
        finderRouter ! link.wikiUrl
      }
    }

  }

}
