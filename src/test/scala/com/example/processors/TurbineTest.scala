package com.example.processors

import java.time.LocalDateTime

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.example._
import org.scalatest._

class TurbineTest extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with FunSuiteLike with Matchers with BeforeAndAfter with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  var turbineId: TurbineId = _
  var turbine: ActorRef = _

  before {
    turbineId = TurbineId("1")
    turbine = childActorOf(Turbine.props(turbineId))
  }

  test("Turbine stops working") {
    val m = broken
    turbine ! m
    expectMsg(EventError(m.timestamp, turbineId, Option.empty, "Turbine is broken", Open))
  }

  def broken = StatusUpdate(LocalDateTime.now(), turbineId, BigDecimal(3000), Broken)
}
