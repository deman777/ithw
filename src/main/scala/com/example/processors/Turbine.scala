package com.example.processors

import akka.actor.{Actor, Props}
import com.example.TurbineId

class Turbine(id: TurbineId) extends Actor {
  override def receive: Receive = PartialFunction.empty
}

object Turbine {
  def props(turbineId: TurbineId): Props = Props(new Turbine(turbineId))
}


