package com.example.processors

import akka.actor.{Actor, Props}
import com.example.{Movement, StatusUpdate}

class Processors extends Actor {
  private val people = context.system.actorOf(People.props, "people")
  private val turbines = context.system.actorOf(Turbines.props, "turbines")

  override def receive: Receive = {
    case statusUpdate: StatusUpdate =>
      turbines.forward(statusUpdate)
    case movement: Movement =>
      people.forward(movement)
      turbines.forward(movement)
  }
}

object Processors {
  val props: Props = Props[Processors]
}
