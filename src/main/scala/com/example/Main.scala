package com.example

import java.time.LocalDateTime

import akka.actor.{ActorRef, ActorSystem}
import akka.stream._
import com.example.Clock.Start
import com.example.emitters.Emitters
import com.example.processors.Processors

import scala.concurrent._

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

  private val processors: ActorRef = system.actorOf(Processors.props, "processors")
  private val emitters: ActorRef = system.actorOf(Emitters.props(processors), "emitters")

  private val clock: ActorRef = system.actorOf(Clock.props(emitters), "clock")
  clock ! Start(LocalDateTime.of(2015, 11, 23, 0, 0))
}
