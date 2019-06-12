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

  def storage(
      storage: NewsStorage,
      sender: ActorRef[TelegramMessages]
  ): Behavior[StorageMessage] = Behaviors.receive { (context, message) =>
    message match {
      case StorageMessage.SaveMessage(items) =>
        context.log.info(s"Receive new news, count: ${items.size}")
        val sendIds = storage.items()
        val newItems = items.filterNot(item => sendIds(item.id))
        val notStopWordsItems = newItems.filterNot(i => StopWordFilter.hasStopWords(i.title))
        context.log.info(s"After filter news, count: ${notStopWordsItems.size}")
        if (notStopWordsItems.nonEmpty) {
          sender ! TelegramMessages.SendNews(notStopWordsItems, context.self)
        }
        Behaviors.same
      case StorageMessage.SendNews(ids) =>
        context.log.info(s"News has been sent, count: ${ids.size}")
        // TODO ttl
        storage.addItems(ids)
        Behaviors.same
    }
  }
}
