package com.example

import akka.actor.{Actor, Props}

class Person extends Actor {
  override def receive: Receive = PartialFunction.empty
}

object Person {
  def props: Props = Props[Person]
}
