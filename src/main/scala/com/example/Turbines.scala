package com.example

import akka.actor.{Actor, Props}

class Turbines extends Actor {

  override def receive: Receive = {
    case m@TurbineStatusUpdate(_, turbineId: TurbineId, _, _) => turbine(turbineId) ! m
    case m@PersonMovement(_, turbineId: TurbineId, _, _) => turbine(turbineId) ! m
  }

  private def turbine(turbineId: TurbineId) = {
    context.child(turbineId.id)
      .getOrElse(context.system.actorOf(Turbine.props(turbineId), turbineId.id))
  }
}

object Turbines {
  val props: Props = Props[Turbines]
}


