package com.example

import akka.actor.{Actor, ActorRef}

class MovementsProcessor(people: ActorRef, turbines: ActorRef) extends Actor {
  override def receive: Receive = {
    case m: PersonMovement =>
      people ! m
      turbines ! m
  }
}
