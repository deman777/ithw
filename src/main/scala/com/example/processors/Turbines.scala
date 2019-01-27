package com.example.processors

import akka.actor.{Actor, Props}
import com.example._

class Turbines extends Actor {

  override def receive: Receive = {
    case m@StatusUpdate(_, turbineId: TurbineId, _, _) => turbine(turbineId) ! m
    case m@Movement(_, turbineId: TurbineId, _, _) => turbine(turbineId) ! m
  }

  private def turbine(turbineId: TurbineId) =
    context.child(turbineId.id)
      .getOrElse(context.system.actorOf(Turbine.props(turbineId), turbineId.id))
}

object Turbines {
  val props: Props = Props[Turbines]
}


