package com.example.processors

import akka.actor.{Actor, Props}
import com.example._

class Turbines extends Actor {

  override def receive: Receive = {
    case m@StatusUpdate(_, turbineId: TurbineId, _) => turbine(turbineId) ! m
    case m@Movement(_, turbineId: TurbineId, _, _) => turbine(turbineId) ! m
    case toReminders: ToReminders => context.parent.forward(toReminders)
    case logError: LogError => context.parent.forward(logError)
  }

  private def turbine(turbineId: TurbineId) = {
    val name = turbineId.id
    context.child(name).getOrElse(context.system.actorOf(Turbine.props(turbineId), name))
  }
}

object Turbines {
  def props: Props = Props[Turbines]
}


