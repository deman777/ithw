package com.example

import akka.actor.{Actor, ActorLogging}
import com.example.Clock.{Start, Tick}
import com.example.emitters.Emitters
import com.example.processors.Processors

class Master extends Actor with ActorLogging {
  private val processors = context.actorOf(Processors.props, "processors")
  private val emitters = context.actorOf(Emitters.props, "emitters")
  private val clock = context.actorOf(Clock.props, "clock")
  private val reminders = context.actorOf(Reminder.props, "reminders")

  override def receive: Receive = {
    case start: Start =>
      log.info("Forwarding start to clock.")
      clock.forward(start)
    case tick: Tick =>
      emitters.forward(tick)
      reminders.forward(tick)
    case event: Event =>
      processors.forward(event)
    case toReminders: ToReminders =>
      reminders.forward(toReminders)
  }
}