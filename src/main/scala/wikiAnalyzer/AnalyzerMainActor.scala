package wikiAnalyzer

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.alpakka.sse.scaladsl.EventSource
import akka.stream.scaladsl.Source
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.NotUsed

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.stream.ThrottleMode
import akka.stream.alpakka.mongodb.scaladsl.MongoSink
import com.mongodb.reactivestreams.client.MongoClients
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

import scala.util.parsing.json.JSON

object AnalyzerMainActor {
  final case class Message(text: String)

  final case class Event(user: String, timestamp: Int, topic: String, contributionType: String, rawEvent: String)

  private val client = MongoClients.create("mongodb://localhost:27017")
  private val db = client.getDatabase("Events")

  val codecRegistry = fromRegistries(fromProviders(classOf[Event]), DEFAULT_CODEC_REGISTRY)

  private val eventColl = db
    .getCollection("events", classOf[Event])
    .withCodecRegistry(codecRegistry)

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
        elements = 10,
        per = 20.seconds,
        maximumBurst = 2,
        ThrottleMode.Shaping
      )
      .map(data => {
        val rawData = data.getData()

        println(JSON.parseFull(rawData))

        JSON.parseFull(rawData) match {
          case Some(json) => {

            val map = json.asInstanceOf[Map[String, Any]]
            val user = map("user").asInstanceOf[String]
            val timestamp = map("timestamp").asInstanceOf[Number].intValue()
            val topic = map("meta").asInstanceOf[Map[String, Any]]("topic").asInstanceOf[String]
            val contributionType = map("type").asInstanceOf[String]

            Event(user, timestamp, topic, contributionType, rawData)
          }
          case None => throw new Exception("Error parsing JSON")
        }
      })
      .runWith(MongoSink.insertOne(eventColl))

    Behaviors.same
  }
}
