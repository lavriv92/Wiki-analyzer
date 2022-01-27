package wikiAnalyzer

import akka.actor.typed.ActorSystem

object WikiAnalyzer extends App {
    ActorSystem(AnalyzerMainActor(), "rootSystem")
}
