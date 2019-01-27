package com.example

import java.time.LocalDateTime

import akka.actor.{Actor, ActorRef}
import com.example.Clock.Tick

final class Reminders extends Actor {

  override def receive: Receive = withState(Seq.empty, LocalDateTime.MIN)

  def withState(reminders: Seq[RemindAt], time: LocalDateTime): Receive = {
    case Tick(newTime) =>
      val (now, later) = reminders.partition(r => r.time.compareTo(newTime) <= 0)
      now.foreach(r => r.who ! r.message)
      context.become(withState(later, newTime))
    case Remind(in, message) =>
      context.become(withState(reminders :+ RemindAt(time.plus(in), sender(), message), time))
    case ClearReminders =>
      context.become(withState(reminders.filterNot(r => r.who == sender()), time))
  }

  private final case class RemindAt(time: LocalDateTime, who: ActorRef, message: Any)
}

trait ToReminder
final case class Remind(in: java.time.Duration, message: Any) extends ToReminder
case object ClearReminders extends ToReminder

final case class Reminder(timestamp: LocalDateTime, message: Any)
