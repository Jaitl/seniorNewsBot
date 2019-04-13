package com.github.jaitl.seniornews.aggregator

import java.net.URL

import com.github.jaitl.seniornews.models.NewsItem
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader

import scala.util.Try

trait RssReader {
  def readUrl(url: String): Try[Seq[NewsItem]]
}

class RomeRssReaderImpl() extends RssReader {
  import scala.collection.JavaConverters._

  override def readUrl(url: String): Try[Seq[NewsItem]] = Try {
    val input = new SyndFeedInput()
    val feed = input.build(new XmlReader(new URL(url)))

    feed.getEntries.asScala.map(e => NewsItem(e.getUri, e.getTitle, e.getLink))
  }
}
