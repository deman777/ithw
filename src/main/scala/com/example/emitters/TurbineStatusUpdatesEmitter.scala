package com.example.emitters

import akka.actor.Props
import com.example._

class TurbineStatusUpdatesEmitter extends AbstractEventsEmitter[TurbineStatusUpdate] {
  override protected val name: String = "turbines"
  override protected val toEvent: Map[String, String] => TurbineStatusUpdate = toStatusUpdate

  private val parseStatus : PartialFunction[String, Status] = {
    case "Working" => Working
    case "Broken" => Broken
  }

  private def toStatusUpdate(map: Map[String, String]): TurbineStatusUpdate = {
    TurbineStatusUpdate(
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
