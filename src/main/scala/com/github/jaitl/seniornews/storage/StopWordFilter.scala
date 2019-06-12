package com.github.jaitl.seniornews.storage

object StopWordFilter {

  private val stopWord: Set[String] = Set(
    "blockchain",
    "cryptocurrency",
    "currency",
    "Data Analysis",
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
    "Getting Started",
    "top",
    "best"
  ).map(_.toLowerCase)

  def splitTitle(title: String): Set[String] = {
    val clearTitle = title.replaceAll("[^A-Za-z0-9]", " ")
    clearTitle.split(" ").filterNot(_.isEmpty).map(_.toLowerCase).toSet
  }

  def hasStopWords(title: String): Boolean = {
    val titleWords = splitTitle(title)
    val res = stopWord.intersect(titleWords)
    res.nonEmpty
  }
}
