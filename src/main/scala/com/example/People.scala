package com.example

import akka.actor.{Actor, Props}

class People extends Actor {

  override def receive: Receive = {
    case m: PersonMovement =>
      context.child(m.person.id).getOrElse(context.system.actorOf(Person.props, m.person.id)) ! m
  }
}

object People {
  val props: Props = Props[People]
}
