package com.example.processors

import akka.actor.{Actor, Props}
import com.example.{LogError, Movement, PersonId}

class People extends Actor {

  override def receive: Receive = {
    case m@Movement(_, _, personId: PersonId, _) => person(personId) ! m
    case logError: LogError => context.parent.forward(logError)
  }

  private def person(personId: PersonId) =
    context.child(personId.id)
      .getOrElse(context.actorOf(Person.props(personId), personId.id))
}

object People {
  val props: Props = Props[People]
}
