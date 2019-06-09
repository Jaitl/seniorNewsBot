package com.github.jaitl.seniornews.storage

object StopWordFilter {

  private val stopWord: Set[String] = Set(
    "blockchain",
    "cryptocurrency",
    "Data Analysis",
    "IoC",
    "AI",
    "Beginners",
    "Node",
    "Node.js",
    "React",
    "ReactJs",
    "Angular",
    "Neural",
    "gdpr",
    "redux",
    "js",
    "Javascript",
    "PHP",
    "Getting Started",
    "top",
    "best"
  ).map(_.toLowerCase)

  def hasStopWords(title: String): Boolean = {
    val lowerTitle = title.toLowerCase
    stopWord.map(w => lowerTitle.contains(w)).reduce(_ || _)
  }
}
