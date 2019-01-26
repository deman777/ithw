package com.example

import akka.actor.{Actor, ActorRef}
import com.example.Clock.Tick

class EventsEmitter(events: Seq[Event], processor: ActorRef) extends Actor {
  override def receive: Receive = withEvents(events)

  private def withEvents(movements: Seq[Event]): Receive = {
    case Tick(time) =>
      val (now, next) = movements.span(_.timestamp.compareTo(time) <= 0)
      now.foreach(processor ! _)
      context.become(withEvents(next))
  }
}
