package com.example

import java.time.LocalDateTime

case class PersonId(id: String)

trait Location {val id: String}
final case class VesselId(id: String) extends Location
final case class TurbineId(id: String) extends Location

trait Direction
case object Exit extends Direction
case object Enter extends Direction

trait Status
case object Working extends Status
case object Broken extends Status

trait Event {
  val timestamp: LocalDateTime
}
case class Movement(
  timestamp: LocalDateTime,
  location: Location,
  person: PersonId,
  direction: Direction
) extends Event

case class StatusUpdate(
  timestamp: LocalDateTime,
  turbine: TurbineId,
  status: Status
) extends Event