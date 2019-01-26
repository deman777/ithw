package com.example

import akka.actor.{Actor, ActorLogging}

class Printer extends Actor with ActorLogging {
  override def receive: Receive = {
    case message =>
      log.info(message.toString)
  }
}
