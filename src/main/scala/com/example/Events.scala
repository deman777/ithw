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
case class PersonMovement(
  timestamp: LocalDateTime,
  location: Location,
  person: PersonId,
  direction: Direction
) extends Event

case class TurbineStatusUpdate(
  timestamp: LocalDateTime,
  turbine: TurbineId,
  activePower: BigDecimal,
  status: Status
) extends Event