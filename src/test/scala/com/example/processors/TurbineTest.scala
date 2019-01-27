package com.example.processors

import java.time.Duration.{ofHours, ofMinutes}
import java.time.LocalDateTime

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import com.example._
import org.scalatest._

import scala.concurrent.duration.Duration.Undefined

class TurbineTest extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with FunSuiteLike with Matchers with BeforeAndAfter with BeforeAndAfterAll with OneInstancePerTest {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  var turbineId: TurbineId = TurbineId("1")
  var personId: PersonId = PersonId("1")
  var turbine: ActorRef = _

  before {
    turbine = childActorOf(Turbine.props(turbineId))
  }

  test("Turbine stops working") {
    val m = broken
    turbine ! m
    expectMsg(EventError(m.timestamp, turbineId, None, "Turbine is broken", Open))
  }

  test("A technician exits a turbine without having repaired the turbine") {
    turbine ! broken
    receiveN(2)

    turbine ! enter
    expectMsg(ClearReminders)

    turbine ! exit
    expectMsg(Remind(ofMinutes(3), IsBrokenAfterTechnician(personId)))

    val timestamp = LocalDateTime.now()
    turbine ! Reminder(timestamp, IsBrokenAfterTechnician(personId))

    expectMsg(EventError(timestamp, turbineId, Some(personId), "Technician did not repair turbine", Open))
  }

  test("A turbine has been in a Broken state for more than 4 hours") {
    turbine ! broken
    receiveOne(Undefined)
    expectMsg(Remind(ofHours(4), IsBrokenFourHours))

    val timestamp = LocalDateTime.now()
    turbine ! Reminder(timestamp, IsBrokenFourHours)

    expectMsg(EventError(timestamp, turbineId, None, "Turbine is broken for more than 4 hours", Open))
  }

  test("Turbine starts working after it is broken") {
    turbine ! broken
    receiveN(2)

    turbine ! working
    expectMsg(ClearReminders)
  }

  test("Turbine starts working after it is broken and technician enters") {
    turbine ! broken
    receiveN(2)

    turbine ! enter
    receiveN(1)

    turbine ! working
    expectMsg(ClearReminders)
  }

  test("Turbine starts working after it is broken and technician enters then exits") {
    turbine ! broken
    receiveN(2)

    turbine ! enter
    receiveN(1)

    turbine ! exit
    receiveN(1)

    turbine ! working
    expectMsg(ClearReminders)
  }

  private def enter = Movement(LocalDateTime.now(), turbineId, personId, Enter)
  private def exit = Movement(LocalDateTime.now(), turbineId, personId, Exit)
  private def broken = StatusUpdate(LocalDateTime.now(), turbineId, Broken)
  private def working = StatusUpdate(LocalDateTime.now(), turbineId, Working)
}
