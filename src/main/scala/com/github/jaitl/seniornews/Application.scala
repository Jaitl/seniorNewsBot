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
    ChannelInfo("https://hnrss.org/newest?points=40", "hackernews"),
    ChannelInfo("https://www.infoworld.com/index.rss", "infoworld"),
    ChannelInfo("https://www.javaworld.com/index.rss", "javaworld")
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
