package com.example

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, Props}

class Logger extends Actor with ActorLogging {
  override def receive: Receive = {
    case error: ErrorEvent =>
      log.error(error.toString)
  }
}

object Logger {
  val props: Props = Props[Logger]
}

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

case class ErrorEvent(
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
       |  "error_state": "$errorState",
       |}
      """.stripMargin
}