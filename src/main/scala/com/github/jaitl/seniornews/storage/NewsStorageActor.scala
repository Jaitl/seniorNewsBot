package com.github.jaitl.seniornews.storage

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.github.jaitl.seniornews.models.NewsItem
import com.github.jaitl.seniornews.telegram.TelegramBotActor.TelegramMessages

object NewsStorageActor {
  sealed trait StorageMessage

  object StorageMessage {
    case class SaveMessage(items: Seq[NewsItem]) extends StorageMessage
    case class SendNews(ids: Set[String]) extends StorageMessage
  }

  def name: String = "NewsStorageActor"

  def storage(sender: ActorRef[TelegramMessages]): Behavior[StorageMessage] = storage(Set.empty, sender)

  def storage(
      sendIds: Set[String],
      sender: ActorRef[TelegramMessages]
  ): Behavior[StorageMessage] = Behaviors.receive { (context, message) =>
    message match {
      case StorageMessage.SaveMessage(items) =>
        context.log.info(s"Receive new news, count: ${items.size}")
        val newItems = items.filterNot(item => sendIds(item.id))
        if (newItems.nonEmpty) {
          sender ! TelegramMessages.SendNews(newItems, context.self)
        }
        Behaviors.same
      case StorageMessage.SendNews(ids) =>
        context.log.info(s"News has been sent, count: ${ids.size}")
        // TODO ttl
        storage(sendIds ++ ids, sender)
    }
  }
}
