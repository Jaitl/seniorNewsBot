package com.github.jaitl.seniornews

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.github.jaitl.seniornews.aggregator.ChannelScheduler
import com.github.jaitl.seniornews.aggregator.ChannelActor
import com.github.jaitl.seniornews.aggregator.RomeRssReaderImpl
import com.github.jaitl.seniornews.storage.NewsStorage
import com.github.jaitl.seniornews.storage.NewsStorageActor
import com.github.jaitl.seniornews.telegram.SubscriberStorage
import com.github.jaitl.seniornews.telegram.TelegramBotActor
import com.github.jaitl.seniornews.telegram.TelegramBotActor.TelegramMessages
import com.typesafe.scalalogging.LazyLogging

object Application extends App with LazyLogging {

  import pureconfig.generic.auto._

  val config = pureconfig.loadConfigOrThrow[Config]("bot")

  val newsStorage = new NewsStorage(config.newsStorage)
  val subscriberStorage = new SubscriberStorage(config.subscriberStorage)

  logger.info(s"Subscribers count: ${subscriberStorage.subscribers().size}")
  logger.info(s"Send news count: ${newsStorage.items().size}")

  val main: Behavior[NotUsed] =
    Behaviors.setup { context =>
      context.log.info("Init main actor")
      val channelReader = new RomeRssReaderImpl

      val telegram = context.spawn(
        behavior = new TelegramBotActor(config.credentials.token, subscriberStorage),
        name = TelegramBotActor.name
      )
      val storage = context.spawn(NewsStorageActor.storage(newsStorage, telegram), NewsStorageActor.name)

      val channels = Channels.rssChannels.map { channel =>
        context.spawn(ChannelActor.channel(channelReader, channel, storage), ChannelActor.name(channel.name))
      }

      context.spawn(ChannelScheduler.scheduler(config.chanelSchedule, channels), ChannelScheduler.name)

      telegram ! TelegramMessages.StartListenMessages

      Behaviors.same
    }

  val system = ActorSystem(main, "SeniorNewsBot")

  sys.addShutdownHook {
    logger.info("Stop bot")
    newsStorage.close()
    subscriberStorage.close()
    system.terminate()
  }
}
