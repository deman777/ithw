package com.example

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, Props}
import com.example.Printer.Error

class Printer extends Actor with ActorLogging {
  override def receive: Receive = {
    case error: Error =>
      log.info(error.toString)
  }
}

object Printer {
  val props: Props = Props[Printer]
  trait ErrorState {
    val name: String
    override def toString: String = name
  }
  case object Open extends ErrorState {
    override val name: String = "open"
  }
  case object Closed extends ErrorState {
    override val name: String = "closed"
  }
  case class Error(
    date: LocalDateTime,
    turbine: TurbineId,
    person: PersonId,
    error: String,
    errorState: ErrorState
  ) {
    override def toString: String =
      s"""
         |{
         |  "date": "$date"
         |  "turbine": "${turbine.id}",
         |  "person": "${person.id}",
         |  "error": "$error",
         |  "error_state": "$errorState",
         |}
      """.stripMargin
  }
}