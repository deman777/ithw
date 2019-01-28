package com.example

import akka.actor.{Actor, ActorLogging}
import com.example.Clock.{Start, Stop, Tick}
import com.example.emitters.Emitters
import com.example.processors.Processors

class Master extends Actor with ActorLogging {
  private val processors = context.actorOf(Processors.props, "processors")
  private val emitters = context.actorOf(Emitters.props, "emitters")
  private val clock = context.actorOf(Clock.props, "clock")
  private val reminders = context.actorOf(Reminders.props, "reminders")
  private val logger = context.actorOf(Logger.props, "logger")

  override def receive: Receive = {

    case start: Start =>
      clock.forward(start)

    case tick: Tick =>
      emitters.forward(tick)
      reminders.forward(tick)

    case event: Event =>
      processors.forward(event)

    case toReminders: ToReminders =>
      reminders.forward(toReminders)

    case logError: LogError =>
      logger.forward(logError)

    case Stop =>
      clock.forward(Stop)
      log.info("here i should shut down gracefully")
  }
}