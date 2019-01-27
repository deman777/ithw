package com.example.processors

import akka.actor.{Actor, Props}
import com.example._

class Turbine extends Actor {
  override def receive: Receive = {
    case m@StatusUpdate(timestamp, turbine, Broken) =>
      context.parent ! EventError(timestamp, turbine, Option.empty, "Turbine is broken", Open)
      context.become(broken(m))
  }

  def broken(lastStatus: StatusUpdate): Receive = {
    case Movement(timestamp, turbineId: TurbineId, person, Exit) =>
      context.parent ! EventError(timestamp, turbineId, Some(person), "Technician did not repair turbine", Open)
  }
}

object Turbine {
  def props: Props = Props[Turbine]
}


