package com.example.emitters

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.example.Clock.{Start, Stop, Tick}
import com.example.Event
import com.example.emitters.AbstractEventsEmitter.{EventsFinished, Read}

import scala.collection.immutable.Set

class MainEmitter extends Actor with ActorLogging {

  log.info("Initializing emitters")

  override def receive: Receive = readingEvents(Set(
    context.actorOf(MovementsEmitter.props, "movements"),
    context.actorOf(TurbineStatusUpdatesEmitter.props, "turbines")
  ), Set.empty, LocalDateTime.MAX)

  private def readingEvents(
    readingEmitters: Set[ActorRef],
    readyEmitters: Set[ActorRef],
    startTimestamp: LocalDateTime
  ): Receive = {
    case read: Read =>
      afterRead(
        readingEmitters - sender(),
        readyEmitters + sender(),
        min(startTimestamp, read.startTimestamp)
      )
  }

  private def afterRead(
    readingEmitters: Set[ActorRef],
    readyEmitters: Set[ActorRef],
    startTimestamp: LocalDateTime
  ): Unit = {
    if (readingEmitters.isEmpty) startEmitting(readyEmitters, startTimestamp)
    else {
      context.become(readingEvents(readingEmitters, readyEmitters, startTimestamp))
    }
  }

  private def startEmitting(readyEmitters: Set[ActorRef], startTimestamp: LocalDateTime): Unit = {
    log.info("All events read. Starting system.")
    context.become(emittingEvents(readyEmitters))
    log.info("Sending start to parent")
    context.parent ! Start(startTimestamp)
  }

  private def emittingEvents(emitting: Set[ActorRef]): Receive = {
    case tick: Tick => context.children.foreach(_.forward(tick))
    case event: Event => context.parent.forward(event)
    case EventsFinished => afterEmitterEventsFinished(emitting - sender())
  }

  private def afterEmitterEventsFinished(emitting: Set[ActorRef]): Unit = {
    if (emitting.isEmpty) {
      log.info("ALL EVENTS FINISHED, notifying parent")
      context.parent ! Stop
    }
    else context.become(emittingEvents(emitting))
  }

  private def min(a: LocalDateTime, b: LocalDateTime) = if (a.compareTo(b) < 0) a else b
}

object MainEmitter {
  val props: Props = Props[MainEmitter]
}