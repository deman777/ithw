package com.example.processors

import java.time.Duration
import java.time.Duration.{ofHours, ofMinutes}

import akka.actor.{Actor, ActorLogging, Props}
import com.example.Logger.{Closed, ErrorEvent, Open}
import com.example.Reminders.{ClearReminders, RemindMe, RemindingYou}
import com.example._

class Turbine(turbineId: TurbineId) extends Actor with ActorLogging {
  override def receive: Receive = {
    case m@StatusUpdate(_, _, Broken) => onBroken(m)
  }

  private def onBroken(statusUpdate: StatusUpdate): Unit = {
    logError(ErrorEvent(statusUpdate.timestamp, turbineId, None, "Turbine is broken", Open))
    remind(ofHours(4), BrokenFourHours)
    context.become({
      case Movement(_, _, _, Enter) => onEnter()
      case r@RemindingYou(_, BrokenFourHours) => onBrokenFourHours(r)
      case StatusUpdate(_, _, Working) => onWorking()
    })
  }

  private def onBrokenFourHours(r: RemindingYou): Unit = {
    logError(ErrorEvent(r.timestamp, turbineId, None, "Turbine is broken for more than 4 hours", Open))
    context.become({
      case Movement(timestamp, _: TurbineId, personId, Enter) =>
        logError(ErrorEvent(timestamp, turbineId, Some(personId), "Turbine is broken for more than 4 hours", Closed))
        onEnter()
      case StatusUpdate(_, _, Working) => onWorking()
    })
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
      case RemindingYou(timestamp, BrokenAfterTechnician(personId)) =>
        logError(ErrorEvent(timestamp, turbineId, Some(personId), "Technician did not repair turbine", Open))
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
    context.parent ! RemindMe(in, message)
  }

  private def logError(errorEvent: ErrorEvent): Unit = {
    context.parent ! errorEvent
  }
}

object Turbine {
  def props(turbineId: TurbineId): Props = Props(new Turbine(turbineId))
}

case class BrokenAfterTechnician(personId: PersonId)
case object BrokenFourHours


