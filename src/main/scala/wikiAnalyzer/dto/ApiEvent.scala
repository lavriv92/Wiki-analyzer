package wikiAnalyzer.dto

case class ApiEvent(_id: String, user: String, timestamp: Int, topic: String, contributionType: String, rawEvent: String)
