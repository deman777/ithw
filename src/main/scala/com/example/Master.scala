package com.example

import akka.actor.Actor
import com.example.Clock.{Start, Tick}
import com.example.emitters.Emitters
import com.example.processors.Processors

import scala.PartialFunction.empty

class Master extends Actor {
  override def receive: Receive = empty
  override def preStart(): Unit = {
    val processors = context.system.actorOf(Processors.props, "processors")
    val emitters = context.system.actorOf(Emitters.props(self), "emitters")
    val clock = context.system.actorOf(Clock.props(self), "clock")
    val reminders = context.system.actorOf(Reminder.props(self), "reminders")
    context.become({
      case start: Start =>
        clock.forward(start)
      case tick: Tick =>
        emitters.forward(tick)
        reminders.forward(tick)
      case event: Event =>
        processors.forward(event)
      case toReminders: ToReminders =>
        reminders.forward(toReminders)
    })
  }
}
