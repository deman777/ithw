package com.example

import java.time.LocalDateTime

import akka.actor.{Actor, ActorRef}
import com.example.Clock._

import scala.concurrent.duration.Duration.Zero
import scala.concurrent.duration.{DurationInt, FiniteDuration}


object Clock {
  final case class Start(time: LocalDateTime)
  final case class Tick(time: LocalDateTime)
  private final case object InnerTick
  private def speedy(duration: FiniteDuration): FiniteDuration = duration * (14.minutes / 7.days).longValue()
}

class Clock(listeners: ActorRef*) extends Actor {

  import context.dispatcher

  override def receive: Receive = {
    case Start(begin) =>
      context.become(started(begin))
      context.system.scheduler.schedule(Zero, speedy(1.minute), self, InnerTick);
  }

  private def started(next: LocalDateTime): Receive = {
    case InnerTick =>
      listeners.foreach(_ ! Tick(next))
      context.become(started(next.plusMinutes(1)))
  }
}
