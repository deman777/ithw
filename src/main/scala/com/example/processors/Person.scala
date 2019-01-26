package com.example.processors

import akka.actor.{Actor, Props}
import com.example.PersonId

class Person(personId: PersonId) extends Actor {
  override def receive: Receive = PartialFunction.empty
}

object Person {
  def props(personId: PersonId): Props = Props(new Person(personId))
}
