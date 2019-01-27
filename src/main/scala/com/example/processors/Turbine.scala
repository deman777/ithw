package com.example.processors

import java.time.Duration.{ofHours, ofMinutes}

import akka.actor.{Actor, Props}
import com.example._

class Turbine(turbineId: TurbineId) extends Actor {
  override def receive: Receive = {
    case m@StatusUpdate(_, _, Broken) => onBroken(m)
  }

  private def onBroken(statusUpdate: StatusUpdate): Unit = {
    context.parent ! EventError(statusUpdate.timestamp, turbineId, None, "Turbine is broken", Open)
    context.parent ! Remind(ofHours(4), IsBrokenFourHours)
    context.become({
      case Movement(_, _, _, Enter) => onEnter()
      case r@Reminder(_, IsBrokenFourHours) => onBrokenFourHours(r)
      case StatusUpdate(_, _, Working) => onWorking()
    })
  }

  private def onBrokenFourHours(r: Reminder): Unit = {
    context.parent ! EventError(r.timestamp, turbineId, None, "Turbine is broken for more than 4 hours", Open)
  }

  private def onEnter(): Unit = {
    clearReminders()
    context.become({
      case m@Movement(_, _, _, Exit) => onExit(m)
      case StatusUpdate(_, _, Working) => onWorking()
    })
  }

  private def onExit(movement: Movement): Unit = {
    context.parent ! Remind(ofMinutes(3), IsBrokenAfterTechnician(movement.personId))
    context.become({
      case Reminder(timestamp, IsBrokenAfterTechnician(personId)) =>
        context.parent ! EventError(timestamp, turbineId, Some(personId), "Technician did not repair turbine", Open)
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
}

object Turbine {
  def props(turbineId: TurbineId): Props = Props(new Turbine(turbineId))
}

case class IsBrokenAfterTechnician(personId: PersonId)
case object IsBrokenFourHours


