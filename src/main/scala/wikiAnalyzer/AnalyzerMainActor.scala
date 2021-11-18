package wikiAnalyzer

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object AnalyzerMainActor {
  final case class Message(text: String)

  //TODO: Replace to more relevant code
  def apply(): Behavior[Message] = Behaviors.receive {
    (context, message: Message) =>
      println(s"message here: $message")

      Behaviors.same
  }
}
