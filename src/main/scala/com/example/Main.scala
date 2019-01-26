package com.example

import java.time.LocalDateTime

import akka.actor.{ActorRef, ActorSystem}
import com.example.Clock.Start
import com.example.emitters.Emitters
import com.example.processors.Processors

object Main extends App {
  private implicit val system: ActorSystem = ActorSystem("main")

  private val processors: ActorRef = system.actorOf(Processors.props, "processors")
  private val emitters: ActorRef = system.actorOf(Emitters.props(processors), "emitters")

  private val clock: ActorRef = system.actorOf(Clock.props(emitters), "clock")
  clock ! Start(LocalDateTime.of(2015, 11, 23, 0, 0))
}
