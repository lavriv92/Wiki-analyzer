package wikiAnalyzer

import akka.actor.typed.ActorSystem

object WikiAnalyzer extends App {
  println("Wiki analyzer running")

  val system: ActorSystem[AnalyzerMainActor.Message] =
    ActorSystem(AnalyzerMainActor(), "rootSystem")

  system ! AnalyzerMainActor.Message(text = "Ping")

  system.terminate()
}
