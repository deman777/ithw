package com.example.emitters

import java.nio.file.Paths

import akka.actor.Actor
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.CsvParsing.lineScanner
import akka.stream.alpakka.csv.scaladsl.CsvToMap.toMapAsStrings
import akka.stream.scaladsl.{FileIO, Sink}
import com.example.Clock.Tick
import com.example.Event
import com.example.emitters.Emitters.Read

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

abstract class AbstractEventsEmitter[E <: Event] extends Actor {
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val dispatcher: ExecutionContextExecutor = context.dispatcher
  override def receive: Receive = PartialFunction.empty

  override def preStart(): Unit = {
    FileIO.fromPath(Paths.get(s"data/$name.csv"))
      .via(lineScanner())
      .via(toMapAsStrings())
      .map(toEvent)
      .log("events reading error")
      .runWith(Sink.seq)
      .onComplete {
        case Success(events) =>
          context.become(withEvents(events))
          context.parent ! Read(events.head.timestamp)
        case Failure(_) =>
          context.system.terminate();
      }
  }

  protected val name: String
  protected val toEvent: Map[String, String] => E

  private def withEvents(events: Seq[Event]): Receive = {
    case Tick(now) =>
      val (nowEvents, futureEvents) = events.span(_.timestamp.compareTo(now) <= 0)
      nowEvents.foreach(context.parent ! _)
      context.become(withEvents(futureEvents))
  }
}