package wikiAnalyzer

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http

import Routes.rootRoute

import scala.io.StdIn


object WikiAnalyzer extends App {
    implicit val system = ActorSystem(AnalyzerMainActor(), "rootSystem")
    implicit val executionContext = system.executionContext

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(rootRoute)

    println("Server now online at http://localhost:8080")

    StdIn.readLine()

    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
}
