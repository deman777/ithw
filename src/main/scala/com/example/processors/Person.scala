package com.example.processors

import java.time.LocalDateTime

import akka.actor.{Actor, Props}
import com.example.ErrorPrinter.{Closed, EventError}
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
              context.parent ! EventError(timestamp, turbineId, personId,
                "Can not enter turbine without leaving ship", Closed)
          }
      }
      context.become(after(movement))
  }
}

object Person {
  def props(personId: PersonId): Props = Props(new Person(personId))
}
