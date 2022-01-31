package wikiAnalyzer

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.alpakka.mongodb.scaladsl.MongoSource
import akka.stream.scaladsl.Sink
import spray.json.DefaultJsonProtocol
import wikiAnalyzer.dto.Event
import akka.http.scaladsl.server.Directives._
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Accumulators._


import scala.io.StdIn
import scala.util.{Success, Failure}

final case class EventsList(events: Seq[Event])

trait JSONSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val messageFormat = jsonFormat5(Event)
    implicit val eventsListFormat = jsonFormat1(EventsList)
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

//        path("top-contributed") {
//            get {
//                val aggregation = List(
//                    group("$topic", sum("totalEvents", "$_id"))
//                )
//
//                val eventSource = MongoSource(Db.eventColl.aggregate(aggregation)).runWith(Sink.seq)
//            }
//        }
    }



    val bindingFuture = Http().newServerAt("localhost", 8080).bind(rootRoute)

    println("Server now online at http://localhost:8080")

    StdIn.readLine()

    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
}
