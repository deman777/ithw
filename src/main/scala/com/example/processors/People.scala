package com.example.processors

import akka.actor.{Actor, Props}
import com.example.{PersonId, PersonMovement}

class People extends Actor {

  override def receive: Receive = {
    case m@PersonMovement(_, _, personId: PersonId, _) => person(personId) ! m
  }

  private def person(personId: PersonId) = {
    val name = personId.id
    context.child(name).getOrElse(context.system.actorOf(Person.props(personId), name))
  }
}

object People {
  val props: Props = Props[People]
}
