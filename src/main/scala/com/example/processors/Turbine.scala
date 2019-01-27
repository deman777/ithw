package com.example.processors

import akka.actor.{Actor, Props}
import com.example.{EventError, Open, StatusUpdate, TurbineId}

class Turbine(id: TurbineId) extends Actor {
  override def receive: Receive = {
    case m: StatusUpdate =>
      context.parent ! EventError(m.timestamp, m.turbine, Option.empty, "Turbine is broken", Open)
  }
}

object Turbine {
  def props(turbineId: TurbineId): Props = Props(new Turbine(turbineId))
}


