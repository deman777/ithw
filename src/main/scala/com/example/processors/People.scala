package com.example.processors

import akka.actor.{Actor, Props}
import com.example.{PersonId, Movement}

class People extends Actor {

  override def receive: Receive = {
    case m@Movement(_, _, personId: PersonId, _) => person(personId) ! m
  }

  private def person(personId: PersonId) = {
    val name = personId.id
    context.child(name).getOrElse(context.system.actorOf(Person.props(personId), name))
  }
}

object People {
  val props: Props = Props[People]
}
