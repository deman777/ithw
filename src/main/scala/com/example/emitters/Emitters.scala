package com.example.emitters

import java.time.LocalDateTime

import akka.actor.{Actor, ActorRef, Props}
import com.example.Clock.{Start, Tick}
import com.example.Event
import com.example.emitters.Emitters.Read

import scala.collection.immutable.Set

class Emitters extends Actor {

  override def receive: Receive = reading(Set(
    context.actorOf(MovementsEmitter.props, "movements"),
    context.actorOf(TurbineStatusUpdatesEmitter.props, "turbines")
  ), LocalDateTime.MAX)

  private def reading(refs: Set[ActorRef], minTimestamp: LocalDateTime): Receive = {
    case read: Read =>
      onRead(refs - sender(), min(minTimestamp, read.minTimestamp))
  }

  private val emitting: Receive = {
    case tick: Tick => context.children.foreach(_.forward(tick))
    case event: Event => context.parent.forward(event)
  }

  def onRead(refs: Set[ActorRef], minTimestamp: LocalDateTime): Unit = {
    if (refs.isEmpty) {
      context.become(emitting)
      context.parent ! Start(minTimestamp)
    }
    else {
      context.become(reading(refs, minTimestamp))
    }
  }

  private def min(a: LocalDateTime, b: LocalDateTime) = if (a.compareTo(b) < 0) a else b
}

object Emitters {
  val props: Props = Props[Emitters]
  case class Read(minTimestamp: LocalDateTime)
}
