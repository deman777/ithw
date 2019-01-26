package com.example

import akka.actor.{Actor, ActorRef, Props}
import com.example.Clock.Tick

class EventsEmitter(events: Seq[Event], processors: ActorRef) extends Actor {
  override def receive: Receive = withEvents(events)

  private def withEvents(events: Seq[Event]): Receive = {
    case Tick(now) =>
      val (nowEvents, futureEvents) = events.span(_.timestamp.compareTo(now) <= 0)
      nowEvents.foreach(processors ! _)
      context.become(withEvents(futureEvents))
  }
}

object EventsEmitter {
  def props(events: Seq[Event], processor: ActorRef): Props =
    Props(new EventsEmitter(events, processor))
}
