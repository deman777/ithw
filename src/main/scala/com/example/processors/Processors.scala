package com.example.processors

import akka.actor.{Actor, Props}
import com.example.Event

class Processors extends Actor {

  override def preStart(): Unit = {
    context.system.actorOf(People.props, "people")
    context.system.actorOf(Turbines.props, "turbines")
  }
  override def receive: Receive = {
    case event: Event => context.children.foreach(_ ! event)
  }
}

object Processors {
  val props: Props = Props[Processors]
}
