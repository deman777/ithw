package com.example.emitters

import java.time.LocalDateTime

import akka.actor.{Actor, ActorRef, Props}
import com.example.Clock.{Start, Tick}
import com.example.Event
import com.example.emitters.Emitters.Read

import scala.PartialFunction.empty
import scala.collection.immutable.Set

class Emitters(master: ActorRef) extends Actor {

  override def receive: Receive = empty

  override def preStart(): Unit = {
    val refs = Set(
      context.actorOf(MovementsEmitter.props, "movements"),
      context.actorOf(TurbineStatusUpdatesEmitter.props, "turbines")
    )
    context.become(reading(refs, LocalDateTime.MAX))
  }

  def onRead(refs: Set[ActorRef], minTimestamp: LocalDateTime): Unit = {
    if (refs.isEmpty) {
      context.become(emitting)
      master ! Start(minTimestamp)
    }
    else {
      context.become(reading(refs, minTimestamp))
    }
  }

  def reading(refs: Set[ActorRef], minTimestamp: LocalDateTime): Receive = {
    case read: Read =>
      onRead(refs - sender(), min(minTimestamp, read.minTimestamp))
  }

  private val emitting: Receive = {
    case tick: Tick => context.children.foreach(_.forward(tick))
    case event: Event => master.forward(event)
  }

  private def min(a: LocalDateTime, b: LocalDateTime) = if (a.compareTo(b) < 0) a else b
}

object Emitters {
  def props(listener: ActorRef): Props = Props(new Emitters(listener))
  case class Read(minTimestamp: LocalDateTime)
}
