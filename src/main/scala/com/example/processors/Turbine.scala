package com.example.processors

import java.time.Duration.{ofHours, ofMinutes}

import akka.actor.{Actor, Props}
import com.example._

class Turbine(turbineId: TurbineId) extends Actor {
  override def receive: Receive = {
    case m@StatusUpdate(_, _, Broken) => onBroken(m)
  }

  def onBrokenFourHours(r: Reminder): Unit = {
    context.parent ! EventError(r.timestamp, turbineId, None, "Turbine is broken for more than 4 hours", Open)
  }

  private def onBroken(statusUpdate: StatusUpdate): Unit = {
    context.parent ! EventError(statusUpdate.timestamp, turbineId, None, "Turbine is broken", Open)
    context.parent ! Remind(ofHours(4), IsBrokenFourHours)
    context.become({
      case m@Movement(_, _, _, Enter) => onEnter(m)
      case r@Reminder(_, IsBrokenFourHours) => onBrokenFourHours(r)
    })
  }

  private def onEnter(movement: Movement): Unit = {
    context.parent ! ClearReminders
    context.become({
      case m@Movement(_, _, _, Exit) => onExit(m)
    })
  }

  private def onExit(movement: Movement): Unit = {
    context.parent ! Remind(ofMinutes(3), IsBrokenAfterTechnician(movement.personId))
    context.become({
      case Reminder(timestamp, IsBrokenAfterTechnician(personId)) =>
        context.parent ! EventError(timestamp, turbineId, Some(personId), "Technician did not repair turbine", Open)
    })
  }

  private def onWorking(statusUpdate: StatusUpdate): Unit = {
    context.parent ! ClearReminders
  }
}

object Turbine {
  def props(turbineId: TurbineId): Props = Props(new Turbine(turbineId))
}

case class IsBrokenAfterTechnician(personId: PersonId)
case object IsBrokenFourHours


