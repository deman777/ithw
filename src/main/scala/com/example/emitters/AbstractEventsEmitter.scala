package com.example.emitters

import java.nio.file.Paths

import akka.actor.Actor
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.CsvParsing.lineScanner
import akka.stream.alpakka.csv.scaladsl.CsvToMap.toMapAsStrings
import akka.stream.scaladsl.{FileIO, Sink}
import com.example.Clock.Tick
import com.example.Event

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

abstract class AbstractEventsEmitter[E <: Event] extends Actor {
  private implicit val materializer: ActorMaterializer = ActorMaterializer()

  protected val name: String
  protected val toEvent: Map[String, String] => E

  private def withEvents(events: Seq[Event]): Receive = {
    case Tick(now) =>
      val (nowEvents, futureEvents) = events.span(_.timestamp.compareTo(now) <= 0)
      nowEvents.foreach(context.parent ! _)
      context.become(withEvents(futureEvents))
  }

  override def receive: Receive = withEvents({
    val source = FileIO.fromPath(Paths.get(s"data/$name.csv"))
      .via(lineScanner())
      .via(toMapAsStrings())
      .log("err")
    Await.result(source.runWith(Sink.seq), 1.second).map(toEvent)
  })
}