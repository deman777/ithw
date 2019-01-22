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

trait Location {val id: String}
case class Vessel(id: String) extends Location
case class Turbine(id: String) extends Location

trait Direction
case object Exit extends Direction
case object Enter extends Direction

case class Person(id: String)

trait Event {val timestamp: LocalDateTime}
case class Movement(timestamp: LocalDateTime, location: Location, person: Person, direction: Direction) extends Event

object StreamsApp extends App {
  implicit val system: ActorSystem = ActorSystem("QuickStart")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  def parseTimestamp(string: String) = {
    LocalDateTime.parse(string, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
  }

  private def toMovement(map: Map[String, String]): Movement = {
    def parseLocation(string: String): Location = string match {
      case _ if string.startsWith("Vessel") => Vessel(string.split("\\s")(1))
      case _ => Turbine(string)
    }

    def parseDirection(string: String): Direction = string match {
      case "Enter" => Enter
      case "Exit" => Exit
    }

    Movement(
      parseTimestamp(map("Date")),
      parseLocation(map("Location")),
      Person(map("Person")),
      parseDirection(map("Movement type"))
    )
  }

  private def fileToMap(name: String) = {
    FileIO.fromPath(Paths.get(s"data/$name.csv"))
      .via(lineScanner())
      .via(toMapAsStrings())
      .log("err")
  }

  private def readFile[T <: Event](name: String, mapper: Map[String, String] => T): immutable.Seq[T] = {
    Await.result(fileToMap(name).runWith(Sink.seq), 1.second).map(mapper)
  }

  private val movements = readFile("movements", toMovement)
  movements.foreach(println)

  system.terminate()
}
