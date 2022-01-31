package wikiAnalyzer

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.alpakka.mongodb.scaladsl.MongoSource
import akka.stream.scaladsl.Sink
import spray.json.DefaultJsonProtocol
import wikiAnalyzer.dto.{Contribution, Event}
import akka.http.scaladsl.server.Directives._
import org.mongodb.scala.model.Aggregates.group
import org.mongodb.scala.model.Accumulators.sum
import akka.stream.scaladsl.Source
import scala.io.StdIn
import scala.util.{Failure, Success}

final case class EventsList(events: Seq[Event])
final case class ContributionList(contributions: Seq[Contribution])

trait JSONSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val messageFormat = jsonFormat5(Event)
    implicit val eventsListFormat = jsonFormat1(EventsList)

    implicit val contributionFormat = jsonFormat2(Contribution)
    implicit val contributionsListFormat = jsonFormat1(ContributionList)
}

object WikiAnalyzer extends App with JSONSupport {
    implicit val system = ActorSystem(AnalyzerMainActor(), "rootSystem")
    implicit val executionContext = system.executionContext

    val rootRoute = concat {
        path("events") {
            get {
                val eventsSource = MongoSource(Db.eventColl.find().limit(100)).runWith(Sink.seq)

                onComplete(eventsSource) {
                    case Success(events) => complete(EventsList(events))
                    case Failure(e) => {
                        println(s"error $e.getMessage")
                        complete(EventsList(Seq()))
                    }

                }
            }
        }

        path("top-contributed") {
            get {

                val eventSource = MongoSource(Db.eventColl.find()).runWith(Sink.seq)


                onComplete(eventSource) {
                    case Success(events) => {

                        val contributions = events.groupBy(_.contributionType).map(pair => Contribution(pair._1, pair._2.length))


                        complete(contributions)
                    }
                }

            }
        }
    }



    val bindingFuture = Http().newServerAt("localhost", 8080).bind(rootRoute)

    println("Server now online at http://localhost:8080")

    StdIn.readLine()

    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())

    def mapEvents(key: String, value: Seq[Event]): Contribution = {
        Contribution(key, value.length)
    }
}
