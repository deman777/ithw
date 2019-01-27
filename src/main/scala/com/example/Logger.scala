package com.example

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, Props}

class Logger extends Actor with ActorLogging {
  override def receive: Receive = {
    case error: LogError =>
      log.error(error.toString)
  }
}

object Logger {
  val props: Props = Props[Logger]
}

trait ErrorState
case object Open extends ErrorState
case object Closed extends ErrorState

case class LogError(
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