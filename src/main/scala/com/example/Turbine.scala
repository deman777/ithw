package com.example

import akka.actor.{Actor, Props}

class Turbine(id: TurbineId) extends Actor {
  override def receive: Receive = PartialFunction.empty
}

object Turbine {
  def props(turbineId: TurbineId): Props = Props(new Turbine(turbineId))
}


