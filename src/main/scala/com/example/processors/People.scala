package com.example.processors

import akka.actor.{Actor, Props}
import com.example.Logger.ErrorEvent
import com.example.{Movement, PersonId}

class People extends Actor {

  override def receive: Receive = {
    case m@Movement(_, _, personId: PersonId, _) => person(personId) ! m
    case logError: ErrorEvent => context.parent.forward(logError)
  }

  private def person(personId: PersonId) = {
    val name = personId.id
    context.child(name).getOrElse(context.actorOf(Person.props(personId), name))
  }
}

object People {
  val props: Props = Props[People]
}
