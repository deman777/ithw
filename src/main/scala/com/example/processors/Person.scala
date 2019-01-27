package com.example.processors

import java.time.LocalDateTime

import akka.actor.{Actor, Props}
import com.example.Closed
import com.example._

class Person(personId: PersonId) extends Actor {

  override def receive: Receive = {
    case movement: Movement => context.become(after(movement))
  }

  def after(last: Movement): Receive = {
    case movement: Movement =>
      last match {
        case Movement(_, _: VesselId, _, Enter) =>
          movement match {
            case Movement(timestamp, turbineId: TurbineId, _, Enter) =>
              error(timestamp, turbineId, "Can not enter turbine without leaving ship")
          }
        case Movement(_, _: VesselId, _, Exit) =>
          movement match {
            case Movement(timestamp, turbineId: TurbineId, _, Exit) =>
              error(timestamp, turbineId, "Can not exit turbine without entering it")
          }
      }
      context.become(after(movement))
  }
  private def error(timestamp: LocalDateTime, turbineId: TurbineId, message: String): Unit = {
    context.parent ! EventError(timestamp, turbineId, Some(personId), message, Closed)
  }
}

object Person {
  def props(personId: PersonId): Props = Props(new Person(personId))
}
