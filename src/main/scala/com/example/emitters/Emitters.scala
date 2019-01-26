package com.example.emitters

import akka.actor.{Actor, ActorRef, Props}
import com.example.Clock.Tick
import com.example.Event

class Emitters(processors: ActorRef) extends Actor {
  override def preStart(): Unit = {
    context.actorOf(MovementsEmitter.props, "movements")
    context.actorOf(TurbineStatusUpdatesEmitter.props, "turbines")
  }
  override def receive: Receive = {
    case tick: Tick => context.children.foreach(_ ! tick)
    case event: Event => processors ! event
  }
}

object Emitters {
  def props(processors: ActorRef): Props = Props(new Emitters(processors))
}
