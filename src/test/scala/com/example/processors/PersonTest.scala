package com.example.processors

import java.time.LocalDateTime

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
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
    person ! enterShip
    val movement = enterTurbine

    person ! movement

    probe.expectMsg(LogError(movement.timestamp, movement.location.asInstanceOf[TurbineId], Some(personId),
      "Can not enter turbine without leaving ship", Closed))
  }

  test("Person exits a turbine without having entered the turbine") {
    person ! exitShip
    val movement = exitTurbine

    person ! movement

    probe.expectMsg(LogError(movement.timestamp, movement.location.asInstanceOf[TurbineId], Some(personId),
      "Can not exit turbine without entering it", Closed))
  }

  test("person can exit ship, enter turbine, exit turbine, enter ship") {
    person ! enterShip
    person ! exitShip
    person ! enterTurbine
    person ! exitTurbine
    person ! enterShip

    probe.expectNoMessage()
  }

  private def enterTurbine = Movement(LocalDateTime.now(), TurbineId("1"), personId, Enter)
  private def exitTurbine = Movement(LocalDateTime.now(), TurbineId("1"), personId, Exit)
  private def enterShip = Movement(LocalDateTime.now, VesselId("1"), personId, Enter)
  private def exitShip = Movement(LocalDateTime.now, VesselId("1"), personId, Exit)
}
