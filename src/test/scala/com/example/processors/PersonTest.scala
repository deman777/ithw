package com.example.processors

import java.time.LocalDateTime

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.example.ErrorPrinter.{Closed, EventError}
import com.example._
import org.scalatest._

class PersonTest extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with FunSuiteLike with Matchers with BeforeAndAfter with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  var probe: TestProbe = _
  var personId: PersonId = _
  var person: ActorRef = _

  before {
    probe = TestProbe()
    personId = PersonId("1")
    person = probe.childActorOf(Person.props(personId))
  }

  test("Person moves onto a turbine without having exited a ship") {
    person ! toShip
    val movement = toTurbine

    person ! movement

    probe.expectMsg(EventError(movement.timestamp, movement.location.asInstanceOf[TurbineId], personId,
      "Can not enter turbine without leaving ship", Closed))
  }

  test("Person exits a turbine without having entered the turbine") {
    person ! exitShip
    val movement = exitTurbine

    person ! movement

    probe.expectMsg(EventError(movement.timestamp, movement.location.asInstanceOf[TurbineId], personId,
      "Can not exit turbine without entering it", Closed))
  }

  test("person can exit ship, enter turbine, exit turbine, enter ship") {
    person ! toShip
    person ! exitShip
    person ! toTurbine
    person ! exitTurbine
    person ! toShip

    probe.expectNoMessage()
  }

  private def toTurbine = Movement(LocalDateTime.now(), TurbineId("1"), personId, Enter)
  private def exitTurbine = Movement(LocalDateTime.now(), TurbineId("1"), personId, Exit)
  private def toShip = Movement(LocalDateTime.now, VesselId("1"), personId, Enter)
  private def exitShip = Movement(LocalDateTime.now, VesselId("1"), personId, Exit)
}
