package com.example

import java.io.{BufferedWriter, FileWriter}
import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, Props}
import com.example.Clock.Stop

class Logger extends Actor with ActorLogging {

  private val writer = new BufferedWriter(new FileWriter("data/errors.txt"))

  override def receive: Receive = {
    case error: ErrorEvent =>
      writer.write(error.toString)
      writer.flush()
    case Stop =>
      writer.close()
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
    s"""|{
        |  "date": "$date"
        |  "turbine": "${turbine.id}",
        |  "person": "${person.map(_.id).getOrElse("")}",
        |  "error": "$error",
        |  "error_state": "$errorState",
        |}
        |""".stripMargin
}