package com.example.emitters

import akka.actor.Props
import com.example._

class TurbineStatusUpdatesEmitter extends AbstractEventsEmitter[StatusUpdate] {
  override protected val name: String = "turbines"
  override protected val toEvent: Map[String, String] => StatusUpdate = toStatusUpdate

  private val parseStatus : PartialFunction[String, Status] = {
    case "Working" => Working
    case "Broken" => Broken
  }

  private def toStatusUpdate(map: Map[String, String]): StatusUpdate = {
    StatusUpdate(
      Time.parse(map("Date"), "yyyy-MM-dd HH:mm:ss"),
      TurbineId(map("ID")),
      BigDecimal(map("ActivePower (MW)")),
      parseStatus(map("Status"))
    )
  }
}

object TurbineStatusUpdatesEmitter {
  val props: Props = Props[TurbineStatusUpdatesEmitter]
}
