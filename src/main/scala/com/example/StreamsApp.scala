package com.example

import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.alpakka.csv.scaladsl.CsvParsing.lineScanner
import akka.stream.alpakka.csv.scaladsl.CsvToMap.toMapAsStrings
import akka.stream.scaladsl._

import scala.concurrent._

trait Location {val id: String}
case class Vessel(id: String) extends Location
case class Turbine(id: String) extends Location

trait Direction
case object Exit extends Direction
case object Enter extends Direction

case class Person(id: String)

trait Event {val timestamp: LocalDateTime}
case class Movement(timestamp: LocalDateTime, location: Location, person: Person, direction: Direction)

object StreamsApp extends App {
  implicit val system: ActorSystem = ActorSystem("QuickStart")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  private def toMovement(map: Map[String, String]): Movement = {
    def toLocation(string: String): Location = string match {
      case _ if string.startsWith("Vessel") => Vessel(string.split("\\s")(1))
      case _ => Turbine(string)
    }

    def toDirection(string: String): Direction = string match {
      case "Enter" => Enter
      case "Exit" => Exit
    }

    def toTimestamp(string: String) = {
      LocalDateTime.parse(string, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
    }

    Movement(
      toTimestamp(m("Date")),
      toLocation(m("Location")),
      Person(m("Person")),
      toDirection(m("Movement type"))
    )
  }

  private def fileToMap(name: String) = {
    FileIO.fromPath(Paths.get(s"data/$name.csv"))
      .via(lineScanner())
      .via(toMapAsStrings())
  }

  private val movementsSource = fileToMap("movements")
    .map(toMovement)
    .log("err")

  movementsSource
    .runForeach(println)
    .onComplete(_ => system.terminate())
}
