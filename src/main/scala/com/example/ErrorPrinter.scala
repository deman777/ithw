package com.example

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, Props}

class ErrorPrinter extends Actor with ActorLogging {
  override def receive: Receive = {
    case error: EventError =>
      log.info(error.toString)
  }
}

object ErrorPrinter {
  val props: Props = Props[ErrorPrinter]
}

trait ErrorState
case object Open extends ErrorState
case object Closed extends ErrorState

case class EventError(
  date: LocalDateTime,
  turbine: TurbineId,
  person: Option[PersonId],
  error: String,
  errorState: ErrorState
) {
  override def toString: String =
    s"""
       |{
       |  "date": "$date"
       |  "turbine": "${turbine.id}",
       |  "person": "${person.map(_.id).getOrElse("")}",
       |  "error": "$error",
       |  "error_state": "${errorState.getClass.getSimpleName.toLowerCase}",
       |}
      """.stripMargin
}