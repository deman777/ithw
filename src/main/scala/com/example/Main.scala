package com.example

import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.actor.{ActorRef, ActorSystem}
import akka.stream._
import akka.stream.alpakka.csv.scaladsl.CsvParsing.lineScanner
import akka.stream.alpakka.csv.scaladsl.CsvToMap.toMapAsStrings
import akka.stream.scaladsl._
import com.example.Clock.Start

import scala.collection.immutable
import scala.concurrent._
import scala.concurrent.duration._

case class PersonId(id: String)

trait Location {val id: String}
case class VesselId(id: String) extends Location
case class TurbineId(id: String) extends Location

trait Direction
case object Exit extends Direction
case object Enter extends Direction

trait Status
case object Working extends Status
case object Broken extends Status

trait Event {val timestamp: LocalDateTime}
case class PersonMovement(timestamp: LocalDateTime, location: Location, person: PersonId, direction: Direction) extends Event
case class TurbineStatusUpdate(timestamp: LocalDateTime, turbine: TurbineId, activePower: BigDecimal, status: Status) extends Event

object Main extends App {
  private implicit val system: ActorSystem = ActorSystem("QuickStart")
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  private def parseTimestamp(string: String, format: String) = {
    LocalDateTime.parse(string, DateTimeFormatter.ofPattern(format))
  }

  private def toMovement(map: Map[String, String]): PersonMovement = {
    def parseLocation(string: String): Location = string match {
      case _ if string.startsWith("Vessel") => VesselId(string.split("\\s")(1))
      case _ => TurbineId(string)
    }
    def parseDirection(string: String): Direction = string match {
      case "Enter" => Enter
      case "Exit" => Exit
    }
    PersonMovement(
      parseTimestamp(map("Date"), "dd.MM.yyyy HH:mm"),
      parseLocation(map("Location")),
      PersonId(map("Person")),
      parseDirection(map("Movement type"))
    )
  }

  private def toStatusUpdate(map: Map[String, String]): TurbineStatusUpdate = {
    def parseStatus(string: String): Status = string match {
      case "Working" => Working
      case "Broken" => Broken
    }
    TurbineStatusUpdate(
      parseTimestamp(map("Date"), "yyyy-MM-dd HH:mm:ss"),
      TurbineId(map("ID")),
      BigDecimal(map("ActivePower (MW)")),
      parseStatus(map("Status"))
    )
  }

  private def readFile[T <: Event](name: String, toEvent: Map[String, String] => T): immutable.Seq[T] = {
    val mapSource = FileIO.fromPath(Paths.get(s"data/$name.csv"))
      .via(lineScanner())
      .via(toMapAsStrings())
      .log("err")
    Await.result(mapSource.runWith(Sink.seq), 1.second).map(toEvent)
  }

  private val movements = readFile("movements", toMovement)
  private val statusUpdates = readFile("turbines", toStatusUpdate)

  private val processors: ActorRef = system.actorOf(Processors.props)

  private val movementsEmitter: ActorRef = system.actorOf(EventsEmitter.props(movements, processors))
  private val turbineStatusUpdatesEmitter: ActorRef = system.actorOf(EventsEmitter.props(statusUpdates, processors))

  private val clock: ActorRef = system.actorOf(Clock.props(movementsEmitter, turbineStatusUpdatesEmitter), "clock")
  clock ! Start(statusUpdates.head.timestamp)
}
