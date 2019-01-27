package com.example.processors

import java.time.Duration.ofMinutes

import akka.actor.{Actor, Props}
import com.example._

class Turbine(turbineId: TurbineId) extends Actor {
  override def receive: Receive = {
    case m@StatusUpdate(timestamp, turbine, Broken) =>
      context.parent ! EventError(timestamp, turbine, Option.empty, "Turbine is broken", Open)
      context.become(broken(m))
  }

  def brokenWithTechnician: Receive = {
    case Movement(_, _, personId, Exit) =>
      context.parent ! Remind(ofMinutes(3), IsBrokenAfterTechnician(personId))
      context.become(brokenAfterTechnician)
  }

  def brokenAfterTechnician: Receive = {
    case Reminder(timestamp, IsBrokenAfterTechnician(personId)) =>
      context.parent ! EventError(timestamp, turbineId, Some(personId), "Technician did not repair turbine", Open)
  }

  def broken(lastStatus: StatusUpdate): Receive = {
    case Movement(_, _, _, Enter) => context.become(brokenWithTechnician)
  }
}

object Turbine {
  def props(turbineId: TurbineId): Props = Props(new Turbine(turbineId))
}

case class IsBrokenAfterTechnician(personId: PersonId)


