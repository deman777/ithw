package com.example.processors

import java.time.LocalDateTime

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.example._
import org.scalatest._

import scala.concurrent.duration.Duration.Undefined

class TurbineTest extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with FunSuiteLike with Matchers with BeforeAndAfter with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  var turbineId: TurbineId = _
  var turbine: ActorRef = _

  before {
    turbineId = TurbineId("1")
    turbine = childActorOf(Turbine.props)
  }

  test("Turbine stops working") {
    val m = broken
    turbine ! m
    expectMsg(EventError(m.timestamp, turbineId, Option.empty, "Turbine is broken", Open))
  }

  test("A technician exits a turbine without having repaired the turbine") {
    turbine ! broken
    receiveOne(Undefined)

    val m = exit
    turbine ! m

    expectMsg(EventError(m.timestamp, turbineId, Some(m.person), "Technician did not repair turbine", Open))
  }

  private def exit = Movement(LocalDateTime.now(), turbineId, PersonId("1"), Exit)
  private def broken = StatusUpdate(LocalDateTime.now(), turbineId, Broken)
}
