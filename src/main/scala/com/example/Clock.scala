package com.example

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, Props, Timers}
import com.example.Clock._

import scala.concurrent.duration.{DurationInt, FiniteDuration}


class Clock extends Actor with ActorLogging with Timers {

  override def receive: Receive = {
    case Start(startTime) =>
      log.info(s"Started at $startTime")
      context.become(started(startTime))
      timers.startPeriodicTimer("", InnerTick, speedy(1.minute))
  }

  private def started(startTime: LocalDateTime): Receive = {
    case InnerTick =>
      context.become(ticking(startTime))
      tick(startTime)
  }

  private def ticking(lastTime: LocalDateTime): Receive = {
    case InnerTick =>
      val newTime = lastTime.plusMinutes(1)
      context.become(ticking(newTime))
      tick(newTime)
    case Stop =>
      timers.cancelAll()
  }

  private def tick(time: LocalDateTime): Unit = {
    context.parent ! Tick(time)
  }
}

object Clock {
  private final case object InnerTick
  private def speedy(duration: FiniteDuration): FiniteDuration = duration * (1.minutes / 7.days).longValue()
  final case class Start(time: LocalDateTime)
  final case class Tick(time: LocalDateTime)
  case object Stop
  val props: Props = Props[Clock]
}
