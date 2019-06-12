package com.github.jaitl.seniornews.storage

object StopWordFilter {

  private val stopWord: Set[String] = Set(
    "blockchain",
    "cryptocurrency",
    "currency",
    "IoC",
    "IoT",
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
    "Django",
    "Javascript",
    "PHP",
    "top",
    "best"
  ).map(_.toLowerCase)

  private val stopPhrases: Set[String] = Set(
    "Data Analysis",
    "Getting Started"
  ).map(_.toLowerCase)

  def splitTitle(title: String): Set[String] = {
    val clearTitle = title.replaceAll("[^A-Za-z0-9]", " ")
    clearTitle.split(" ").filterNot(_.isEmpty).map(_.toLowerCase).toSet
  }

  def hasStopWords(title: String): Boolean = {
    val lowerTitle = title.toLowerCase
    val titleWords = splitTitle(lowerTitle)
    val stopWordsInTitle = stopWord.intersect(titleWords)
    val stopPhrasesInTitle = stopPhrases.map(p => lowerTitle.contains(p)).reduce(_ || _)

    stopWordsInTitle.nonEmpty || stopPhrasesInTitle
  }
}
