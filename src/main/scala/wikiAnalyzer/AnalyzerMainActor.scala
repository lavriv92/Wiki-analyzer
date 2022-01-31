package wikiAnalyzer

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.alpakka.sse.scaladsl.EventSource
import akka.stream.scaladsl.Source
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.NotUsed
import akka.http.scaladsl.unmarshalling.Unmarshal

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.stream.ThrottleMode
import akka.stream.scaladsl.Sink

import scala.util.parsing.json.JSON

object AnalyzerMainActor {
  final case class Message(text: String)

  final case class Event(user: String, timestamp: String, topic: String, contributionType: String, rawEvent: String)

  //TODO: Replace to more relevant code
  def apply(): Behavior[Message] = Behaviors.setup { (context) =>
    implicit val system = context.system
    implicit val executionContext = system.executionContext

    val send: HttpRequest => Future[HttpResponse] = Http().singleRequest(_)
    val eventSource: Source[ServerSentEvent, NotUsed] = EventSource(
      uri = "https://stream.wikimedia.org/v2/stream/recentchange",
      send,
      retryDelay = 1.second
    )
    eventSource
      .throttle(
        elements = 1,
        per = 5.seconds,
        maximumBurst = 2,
        ThrottleMode.Shaping
      )
      .take(10)
      .runWith(Sink.seq)
      .map(_.map(data => {
        val rawData = data.getData()
        JSON.parseFull(rawData) match {
          case Some(json) => {
            print(json)
          }
          case None => println("Error parsing JSON")
        }
      }))

    Behaviors.same
  }
}
