package wikiAnalyzer

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import spray.json._

final case class Message(id: Int, text: String)
final case class MessageList(messages: List[Message])

trait JSONSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val messageFormat = jsonFormat2(Message)
  implicit val messageListFormat = jsonFormat1(MessageList)
}

object Routes extends Directives with JSONSupport {
  val rootRoute = path("events") {
    get {
      complete {
        MessageList {
          List (
            Message(1, "Text"),
            Message(2, "Text 2"),
            Message(3, "Text 3")
          )
        }
      }
    }
  }
}
