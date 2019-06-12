package com.github.jaitl.seniornews

import com.github.jaitl.seniornews.models.ChannelInfo

object Channels {

  val rssChannels: Seq[ChannelInfo] = Seq(
    ChannelInfo("https://feed.infoq.com", "infoq"),
    ChannelInfo("http://feeds.dzone.com/home", "dzone"),
    ChannelInfo("https://hnrss.org/newest?points=100", "hackerNews"),
    ChannelInfo("https://sdtimes.com/feed/", "sdTimes"),
    ChannelInfo(
      "https://softwareengineeringdaily.com/category/all-episodes/exclusive-content/articles/feed/",
      "softwareengIneeringDaily"
    ),
    ChannelInfo("https://www.developer.com/developer/dev-25.xml", "developerCom"),
    // blogs
    ChannelInfo("http://feeds.hanselman.com/ScottHanselman", "ScottHanselmanBlog"),
    ChannelInfo("http://feeds.feedburner.com/GDBcode", "GoogleDevBlog"),
    // podcasts
    ChannelInfo("http://feeds.rucast.net/radio-t", "radiotPodcast"),
    ChannelInfo("https://feeds.podcastmirror.com/razborpoletov", "razborPoletovPodcast"),
    ChannelInfo("https://devzen.ru/feed/", "devzenPodcast"),
    ChannelInfo("http://feeds.soundcloud.com/users/soundcloud:users:291337106/sounds.rss", "podlodkaPodcast"),
    ChannelInfo("https://scalalaz.ru/rss/feed.xml", "scalalazPodcast"),
    // rus
    ChannelInfo("https://jug.ru/feed/", "jug"),
    ChannelInfo("https://news.radio-t.com/rss", "radiotNews"),
    ChannelInfo("https://habr.com/ru/rss/hub/java/all/?fl=ru%2Cen", "habrJava"),
    ChannelInfo("https://habr.com/ru/rss/hub/programming/all/?fl=ru%2Cen", "habrDev"),
    // medium
    ChannelInfo("https://hackernoon.com/feed", "hackerNoon"),
    ChannelInfo("https://medium.freecodecamp.org/feed", "freeCodeCamp")
  )
}
