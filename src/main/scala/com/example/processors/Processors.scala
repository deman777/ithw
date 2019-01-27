package com.example.processors

import akka.actor.{Actor, Props}
import com.example.{LogError, Movement, StatusUpdate, ToReminders}

class Processors extends Actor {
  private val people = context.system.actorOf(People.props, "people")
  private val turbines = context.system.actorOf(Turbines.props, "turbines")

  override def receive: Receive = {
    case statusUpdate: StatusUpdate =>
      turbines.forward(statusUpdate)
    case movement: Movement =>
      people.forward(movement)
      turbines.forward(movement)
    case toReminders: ToReminders =>
      context.parent.forward(toReminders)
    case logError: LogError =>
      context.parent.forward(logError)
  }
}

object Processors {
  val props: Props = Props[Processors]
}
