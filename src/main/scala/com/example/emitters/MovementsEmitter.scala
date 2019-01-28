package com.example.emitters

import akka.actor.Props
import com.example._

class MovementsEmitter extends AbstractEventsEmitter[Movement] {
  protected override val name: String = "movements"
  protected override val toEvent: Map[String, String] => Movement = toMovement

  private def parseLocation(location: String) = location match {
    case _ if location.startsWith("Vessel") => VesselId(location.split("\\s")(1))
    case _ => TurbineId(location)
  }

  private val parseDirection: PartialFunction[String, Direction] = {
    case "Enter" => Enter
    case "Exit" => Exit
  }

  private def toMovement(data: Map[String, String]) = {
    Movement(
      parseTimestamp(data("Date"), "dd.MM.yyyy HH:mm"),
      parseLocation(data("Location")),
      PersonId(data("Person")),
      parseDirection(data("Movement type"))
    )
  }
}

object MovementsEmitter {
  val props: Props = Props[MovementsEmitter]
}