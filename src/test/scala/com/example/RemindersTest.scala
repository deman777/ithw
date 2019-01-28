package com.example

import java.time.Duration.ofMinutes
import java.time.LocalDateTime

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.example.Clock.Tick
import com.example.Reminders.{ClearReminders, RemindMe, RemindingYou}
import org.scalatest._

class RemindersTest extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with FunSuiteLike
  with Matchers
  with BeforeAndAfterAll
  with OneInstancePerTest {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private val startTime = LocalDateTime.now()
  private val reminder = childActorOf(Props(new Reminders))
  reminder ! Tick(startTime)

  test("tick reminds correct events") {
    val p1 = TestProbe()
    p1.send(reminder, RemindMe(ofMinutes(1), Message(1)))
    val p2 = TestProbe()
    p2.send(reminder, RemindMe(ofMinutes(2), Message(2)))

    val t1 = startTime.plus(ofMinutes(1))
    reminder ! Tick(t1)
    p1.expectMsg(RemindingYou(t1, Message(1)))

    val t2 = startTime.plus(ofMinutes(2))
    reminder ! Tick(t2)
    p2.expectMsg(RemindingYou(t2, Message(2)))
  }

  test("reminders can be cleared") {
    val p1 = TestProbe()
    p1.send(reminder, RemindMe(ofMinutes(1), Message(1)))
    val p2 = TestProbe()
    p2.send(reminder, RemindMe(ofMinutes(1), Message(2)))

    p1.send(reminder, ClearReminders)

    val t1: LocalDateTime = startTime.plus(ofMinutes(1))
    reminder ! Tick(t1)
    p1.expectNoMessage()
    p2.expectMsg(RemindingYou(t1, Message(2)))
  }

  private case class Message(n: Int)
}
