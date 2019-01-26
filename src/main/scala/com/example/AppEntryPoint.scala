package com.example

import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.alpakka.csv.scaladsl.CsvParsing.lineScanner
import akka.stream.alpakka.csv.scaladsl.CsvToMap.toMapAsStrings
import akka.stream.scaladsl._

import scala.collection.immutable
import scala.concurrent._
import scala.concurrent.duration._

case class Person(id: String)

trait Location {val id: String}
case class Vessel(id: String) extends Location
case class Turbine(id: String) extends Location

trait Direction
case object Exit extends Direction
case object Enter extends Direction

trait Status
case object Working extends Status
case object Broken extends Status

trait Event {val timestamp: LocalDateTime}
case class PersonMovement(timestamp: LocalDateTime, location: Location, person: Person, direction: Direction) extends Event
case class TurbineStatusUpdate(timestamp: LocalDateTime, turbine: Turbine, activePower: BigDecimal, status: Status) extends Event

object AppEntryPoint extends App {
  private implicit val system: ActorSystem = ActorSystem("QuickStart")
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  private def parseTimestamp(string: String, format: String) = {
    LocalDateTime.parse(string, DateTimeFormatter.ofPattern(format))
  }

  private def toMovement(map: Map[String, String]): PersonMovement = {
    def parseLocation(string: String): Location = string match {
      case _ if string.startsWith("Vessel") => Vessel(string.split("\\s")(1))
      case _ => Turbine(string)
    }
    def parseDirection(string: String): Direction = string match {
      case "Enter" => Enter
      case "Exit" => Exit
    }
    PersonMovement(
      parseTimestamp(map("Date"), "dd.MM.yyyy HH:mm"),
      parseLocation(map("Location")),
      Person(map("Person")),
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
      Turbine(map("ID")),
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

  movements.foreach(println)
  statusUpdates.foreach(println)

  system.terminate()
}
