package wikiAnalyzer

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.alpakka.mongodb.scaladsl.MongoSource
import akka.stream.scaladsl.Sink
import spray.json.DefaultJsonProtocol
import wikiAnalyzer.dto.Event
import akka.http.scaladsl.server.Directives._

import java.time.{LocalDate}
import java.time.format.DateTimeFormatter
import scala.io.StdIn
import scala.util.{Failure, Success}
import java.sql.Timestamp

final case class EventsList(events: Seq[Event])

trait JSONSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val messageFormat = jsonFormat5(Event)
    implicit val eventsListFormat = jsonFormat1(EventsList)
}


object WikiAnalyzer extends App with JSONSupport {
    implicit val system = ActorSystem(AnalyzerMainActor(), "rootSystem")
    implicit val executionContext = system.executionContext

    def getTimestamp( dateString: String ) : Long = {
        val formatter = DateTimeFormatter.ofPattern("M/dd/yyyy")
        val startTimestamp = LocalDate.parse(dateString, formatter).atStartOfDay()
        val timestamp = Timestamp.valueOf(startTimestamp)
        timestamp.getTime()
    }

    val rootRoute = concat {
        path("events") {
            get {
                parameters(Symbol("user").as[String], Symbol("start").as[String], Symbol("end").as[String]) { (user, start, end) =>
                    val eventsSource = MongoSource(Db.eventColl.find().limit(100))
                      .filter(event => event.user == user)
                      .filter(event => getTimestamp(event.date) > getTimestamp(start))
                      .filter(event => getTimestamp(event.date) < getTimestamp(end))
                      .runWith(Sink.seq)

                    println(getTimestamp(start));
                    println(getTimestamp(end))

                    onComplete(eventsSource) {
                        case Success(events) => complete(EventsList(events))
                        case Failure(e) => {
                            println(s"error $e.getMessage")
                            complete(EventsList(Seq()))
                        }
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
