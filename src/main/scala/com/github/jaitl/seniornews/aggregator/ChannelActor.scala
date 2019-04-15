package com.github.jaitl.seniornews.aggregator

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.github.jaitl.seniornews.models.ChannelInfo
import com.github.jaitl.seniornews.storage.NewsStorageActor.StorageMessage

import scala.util.Failure
import scala.util.Success

object ChannelActor {
  sealed trait ChannelMessage

  object ChannelMessage {
    case object ScheduleMessage extends ChannelMessage
  }

  def name(name: String): String = s"ChannelActor-$name"

  def channel(
      reader: ChannelReader,
      channelInfo: ChannelInfo,
      sender: ActorRef[StorageMessage]
  ): Behavior[ChannelMessage] = Behaviors.receive { (context, message) =>
    reader.readUrl(channelInfo.url) match {
      case Success(items) =>
        sender ! StorageMessage.SaveMessage(items)
      case Failure(ex) =>
        context.log.error(ex, "Fail to request news from channel")
    }

    Behaviors.same
  }
}
