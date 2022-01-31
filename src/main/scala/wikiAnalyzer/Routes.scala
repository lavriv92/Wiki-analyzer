package wikiAnalyzer

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import spray.json._

final case class Event(id: Int, text: String)
final case class EventList(messages: List[Event])

trait JSONSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val messageFormat = jsonFormat2(Event)
  implicit val messageListFormat = jsonFormat1(EventList)
}

object Routes extends Directives with JSONSupport {
  val rootRoute = path("events") {
    get {
      complete {
        EventList {
          List (
            Event(1, "Text"),
            Event(2, "Text 2"),
            Event(3, "Text 3")
          )
        }
      }
    }
  }
}
