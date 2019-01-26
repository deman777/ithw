package com.example

import akka.actor.{ActorSystem, Props}

object Main extends App {
  private implicit val system: ActorSystem = ActorSystem("main")
  system.actorOf(Props[Master], "master")
}
