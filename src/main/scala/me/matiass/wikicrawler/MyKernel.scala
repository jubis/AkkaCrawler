package me.matiass.wikicrawler

import akka.actor.{Props, ActorSystem}
import akka.kernel.Bootable

/**
 * Kernel to be used if deployed as standalone.
 * Set this as main class in build.sbt.
 */
class MyKernel extends Bootable {
  var actorSystem: ActorSystem = null

  def startup = {
    actorSystem = ApplicationMain.startup
  }

  def shutdown = {
    actorSystem.shutdown()
  }
}
