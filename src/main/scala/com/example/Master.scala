package com.example

import akka.actor.{Actor, ActorRef}
import com.example.Clock.{Start, Tick}
import com.example.emitters.Emitters
import com.example.processors.Processors

import scala.PartialFunction.empty

class Master extends Actor {
  override def receive: Receive = empty
  override def preStart(): Unit = {
    val processors: ActorRef = context.system.actorOf(Processors.props, "processors")
    val emitters: ActorRef = context.system.actorOf(Emitters.props(self), "emitters")
    val clock: ActorRef = context.system.actorOf(Clock.props(self), "clock")
    context.become({
      case start: Start =>
        clock.forward(start)
      case tick: Tick =>
        emitters.forward(tick)
      case event: Event =>
        processors.forward(event)
    })
  }
}
