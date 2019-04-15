package com.github.jaitl.seniornews

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.github.jaitl.seniornews.aggregator.ChannelScheduler
import com.github.jaitl.seniornews.aggregator.ChannelActor
import com.github.jaitl.seniornews.aggregator.RomeRssReaderImpl
import com.github.jaitl.seniornews.models.ChannelInfo
import com.github.jaitl.seniornews.sender.NewsSender
import com.github.jaitl.seniornews.storage.NewsStorageActor

object Application extends App {

  import pureconfig.generic.auto._

  val config = pureconfig.loadConfig[Config]("bot") match {
    case Right(cfg) => cfg
    case Left(er)   => throw new RuntimeException(er.toList.mkString(","))
  }

  val rssChannels = Seq(
    ChannelInfo("https://feed.infoq.com", "infoq"),
    ChannelInfo("http://feeds.dzone.com/home", "dzone")
  )

  val main: Behavior[NotUsed] =
    Behaviors.setup { context =>
      context.log.info("Init main actor")
      val channelReader = new RomeRssReaderImpl

      val sender = context.spawn(NewsSender.sender, NewsSender.name)
      val storage = context.spawn(NewsStorageActor.storage(sender), NewsStorageActor.name)

      val channels = rssChannels.map { channel =>
        context.spawn(ChannelActor.channel(channelReader, channel, storage), ChannelActor.name(channel.name))
      }

      context.spawn(ChannelScheduler.scheduler(config.chanelSchedule, channels), ChannelScheduler.name)

      Behaviors.same
    }

  val system = ActorSystem(main, "SeniorNewsBot")
}
