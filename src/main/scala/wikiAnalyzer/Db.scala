package wikiAnalyzer

import com.mongodb.reactivestreams.client.MongoClients
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._


import dto._

object Db {
  private val client = MongoClients.create("mongodb://localhost:27017")
  private val db = client.getDatabase("Events")

  private val codecRegistry = fromRegistries(fromProviders(classOf[Event]), DEFAULT_CODEC_REGISTRY)

  val eventColl = db
    .getCollection("events", classOf[Event])
    .withCodecRegistry(codecRegistry)
}
