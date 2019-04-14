package com.github.jaitl.seniornews

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.github.jaitl.seniornews.aggregator.ChannelScheduler
import com.github.jaitl.seniornews.aggregator.ChanelSchedulerConfig
import com.github.jaitl.seniornews.aggregator.ChannelActor
import com.github.jaitl.seniornews.aggregator.RomeRssReaderImpl
import com.github.jaitl.seniornews.models.ChannelInfo
import com.github.jaitl.seniornews.sender.NewsSender

object Application extends App {

  import scala.concurrent.duration._

  val rssChannels = Seq(
    ChannelInfo("https://feed.infoq.com", "infoq"),
    ChannelInfo("http://feeds.dzone.com/home", "dzone")
  )

  val schedulerConfig = ChanelSchedulerConfig(1.minutes)

  val main: Behavior[NotUsed] =
    Behaviors.setup { context =>
      context.log.info("init main actor")
      val channelReader = new RomeRssReaderImpl

      val sender = context.spawn(NewsSender.sender, NewsSender.name)

      val channels = rssChannels.map { channel =>
        context.spawn(ChannelActor.channel(channelReader, channel, sender), ChannelActor.name(channel.name))
      }

      context.spawn(ChannelScheduler.scheduler(schedulerConfig, channels), ChannelScheduler.name)

      Behaviors.same
    }

  val system = ActorSystem(main, "SeniorNewsBot")
}
