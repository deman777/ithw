package com.example.processors

import akka.actor.{Actor, Props}
import com.example.Logger.ErrorEvent
import com.example.Reminders.ToReminders
import com.example.{Movement, StatusUpdate}

class Processors extends Actor {

  private val people = context.actorOf(People.props, "people")
  private val turbines = context.actorOf(Turbines.props, "turbines")

  override def receive: Receive = {
    case statusUpdate: StatusUpdate =>
      turbines.forward(statusUpdate)
    case movement: Movement =>
      people.forward(movement)
      turbines.forward(movement)
    case toReminders: ToReminders =>
      context.parent.forward(toReminders)
    case logError: ErrorEvent =>
      context.parent.forward(logError)
  }
}

object Processors {
  val props: Props = Props[Processors]
}
