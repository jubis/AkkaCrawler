package me.matiass.wikicrawler

import akka.actor.{ActorSystem, Props}
import me.matiass.wikicrawler.Messages.Link

object ApplicationMain extends App {
  startup

  def startup = {
    val system = ActorSystem("MyActorSystem")
    val hub = system.actorOf(Props[Hub], "hub")
    hub ! Link("Finland")

    system
  }

}