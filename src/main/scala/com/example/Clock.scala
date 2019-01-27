package com.example

import java.time.LocalDateTime

import akka.actor.{Actor, Props}
import com.example.Clock._

import scala.concurrent.duration.Duration.Zero
import scala.concurrent.duration.{DurationInt, FiniteDuration}


class Clock extends Actor {

  import context.dispatcher

  override def receive: Receive = {
    case Start(startTime) =>
      context.become(started(startTime))
      context.system.scheduler.schedule(Zero, speedy(1.minute), self, InnerTick);
  }

  private def started(startTime: LocalDateTime): Receive = {
    case InnerTick =>
      context.become(ticking(startTime))
      context.parent ! Tick(startTime)
  }

  private def ticking(lastTime: LocalDateTime): Receive = {
    case InnerTick =>
      val newTime = lastTime.plusMinutes(1)
      context.become(ticking(newTime))
      context.parent ! Tick(newTime)
  }
}

object Clock {
  final case class Start(time: LocalDateTime)
  final case class Tick(time: LocalDateTime)
  private final case object InnerTick
  private def speedy(duration: FiniteDuration): FiniteDuration = duration * (14.minutes / 7.days).longValue()
  val props: Props = Props[Clock]
}
