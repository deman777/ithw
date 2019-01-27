package com.example.processors

import akka.actor.{Actor, ActorRef, Props}
import com.example._

class Turbines(master: ActorRef) extends Actor {

  override def receive: Receive = {
    case m@StatusUpdate(_, turbineId: TurbineId, _) => turbine(turbineId) ! m
    case m@Movement(_, turbineId: TurbineId, _, _) => turbine(turbineId) ! m
  }

  private def turbine(turbineId: TurbineId) = {
    val name = turbineId.id
    context.child(name).getOrElse(context.system.actorOf(Turbine.props(turbineId), name))
  }
}

object Turbines {
  def props(master: ActorRef): Props = Props(new Turbines(master))
}


