package wikiAnalyzer.dto

final case class Event(user: String, date: String, topic: String, contributionType: String, rawEvent: String)
