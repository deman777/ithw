package com.example

import java.time.{Duration, LocalDateTime}

import akka.actor.{Actor, ActorRef, Props}
import com.example.Clock.Tick
import com.example.Reminders.{ClearReminders, RemindMe, RemindingYou}

final class Reminders extends Actor {

  override def receive: Receive = withState(Seq.empty, LocalDateTime.MIN)

  def withState(reminders: Seq[RemindAt], time: LocalDateTime): Receive = {
    case Tick(newTime) =>
      val (now, later) = reminders.partition(r => r.time.compareTo(newTime) <= 0)
      now.foreach { remindAt =>
        remindAt.who ! RemindingYou(newTime, remindAt.message)
      }
      context.become(withState(later, newTime))
    case RemindMe(in, message) =>
      context.become(withState(reminders :+ RemindAt(time.plus(in), sender(), message), time))
    case ClearReminders =>
      context.become(withState(reminders.filterNot(r => r.who == sender()), time))
  }
  private final case class RemindAt(time: LocalDateTime, who: ActorRef, message: Any)
}

object Reminders {
  val props: Props = Props[Reminders]
  trait ToReminders
  final case class RemindMe(in: Duration, message: Any) extends ToReminders
  final case class RemindingYou(timestamp: LocalDateTime, message: Any)
  case object ClearReminders extends ToReminders
}
