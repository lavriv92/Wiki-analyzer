package wikiAnalyzer.dto

final case class Event(user: String, timestamp: Number, topic: String, contributionType: String, rawEvent: String)
