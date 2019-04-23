package com.github.jaitl.seniornews

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.github.jaitl.seniornews.aggregator.ChannelScheduler
import com.github.jaitl.seniornews.aggregator.ChannelActor
import com.github.jaitl.seniornews.aggregator.RomeRssReaderImpl
import com.github.jaitl.seniornews.models.ChannelInfo
import com.github.jaitl.seniornews.storage.NewsStorageActor
import com.github.jaitl.seniornews.telegram.TelegramBotActor
import com.github.jaitl.seniornews.telegram.TelegramBotActor.TelegramMessages

object Application extends App {

  import pureconfig.generic.auto._

  val config = pureconfig.loadConfigOrThrow[Config]("bot")

  val rssChannels = Seq(
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
    ChannelInfo("http://feeds.feedburner.com/Baeldung", "baeldungBlog"),
    ChannelInfo("https://springframework.guru/feed/", "springGuruBlog"),
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

  val main: Behavior[NotUsed] =
    Behaviors.setup { context =>
      context.log.info("Init main actor")
      val channelReader = new RomeRssReaderImpl
      val telegram = context.spawn(new TelegramBotActor(config.credentials.token), TelegramBotActor.name)
      val storage = context.spawn(NewsStorageActor.storage(telegram), NewsStorageActor.name)

      val channels = rssChannels.map { channel =>
        context.spawn(ChannelActor.channel(channelReader, channel, storage), ChannelActor.name(channel.name))
      }

      context.spawn(ChannelScheduler.scheduler(config.chanelSchedule, channels), ChannelScheduler.name)

      telegram ! TelegramMessages.StartListenMessages

      Behaviors.same
    }

  val system = ActorSystem(main, "SeniorNewsBot")
}
