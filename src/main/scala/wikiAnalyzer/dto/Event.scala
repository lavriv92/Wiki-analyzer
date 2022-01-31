package wikiAnalyzer.dto

final case class Event(user: String, timestamp: Int, topic: String, contributionType: String, rawEvent: String)
