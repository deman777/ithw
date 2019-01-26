package com.example

import akka.actor.{Actor, ActorRef, Props}
import com.example.Clock.Tick

class EventsEmitter(events: Seq[Event], processor: ActorRef) extends Actor {
  override def receive: Receive = withEvents(events)

  private def withEvents(events: Seq[Event]): Receive = {
    case Tick(time) =>
      val (now, next) = events.span(_.timestamp.compareTo(time) <= 0)
      now.foreach(processor ! _)
      context.become(withEvents(next))
  }
}

object EventsEmitter {
  def props(events: Seq[Event], processor: ActorRef): Props =
    Props(new EventsEmitter(events, processor))
}
