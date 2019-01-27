package com.example.processors

import java.time.Duration
import java.time.Duration.{ofHours, ofMinutes}

import akka.actor.{Actor, Props}
import com.example._

class Turbine(turbineId: TurbineId) extends Actor {
  override def receive: Receive = {
    case m@StatusUpdate(_, _, Broken) => onBroken(m)
  }

  private def onBroken(statusUpdate: StatusUpdate): Unit = {
    reportError(EventError(statusUpdate.timestamp, turbineId, None, "Turbine is broken", Open))
    remind(ofHours(4), BrokenFourHours)
    context.become({
      case Movement(_, _, _, Enter) => onEnter()
      case r@Reminder(_, BrokenFourHours) => onBrokenFourHours(r)
      case StatusUpdate(_, _, Working) => onWorking()
    })
  }

  private def onBrokenFourHours(r: Reminder): Unit = {
    reportError(EventError(r.timestamp, turbineId, None, "Turbine is broken for more than 4 hours", Open))
  }

  private def onEnter(): Unit = {
    clearReminders()
    context.become({
      case m@Movement(_, _, _, Exit) => onExit(m)
      case StatusUpdate(_, _, Working) => onWorking()
    })
  }

  private def onExit(movement: Movement): Unit = {
    remind(ofMinutes(3), BrokenAfterTechnician(movement.personId))
    context.become({
      case Reminder(timestamp, BrokenAfterTechnician(personId)) =>
        reportError(EventError(timestamp, turbineId, Some(personId), "Technician did not repair turbine", Open))
      case StatusUpdate(_, _, Working) => onWorking()
    })
  }

  private def onWorking(): Unit = {
    clearReminders()
    context.become({
      case m@StatusUpdate(_, _, Broken) => onBroken(m)
    })
  }

  private def clearReminders(): Unit = {
    context.parent ! ClearReminders
  }

  private def remind(in: Duration, message: Any): Unit = {
    context.parent ! Remind(in, message)
  }

  private def reportError(eventError: EventError): Unit = {
    context.parent ! eventError
  }
}

object Turbine {
  def props(turbineId: TurbineId): Props = Props(new Turbine(turbineId))
}

case class BrokenAfterTechnician(personId: PersonId)
case object BrokenFourHours


