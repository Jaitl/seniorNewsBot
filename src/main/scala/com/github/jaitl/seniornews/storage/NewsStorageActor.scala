package com.github.jaitl.seniornews.storage

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.github.jaitl.seniornews.models.NewsItem
import com.github.jaitl.seniornews.sender.NewsSender.SenderMessage

object NewsStorageActor {
  sealed trait StorageMessage

  object StorageMessage {
    case class SaveMessage(items: Seq[NewsItem]) extends StorageMessage
  }

  def name: String = "NewsStorageActor"

  def storage(sender: ActorRef[SenderMessage]): Behavior[StorageMessage] = storage(Set.empty, sender)

  def storage(
      sendIds: Set[String],
      sender: ActorRef[SenderMessage]
  ): Behavior[StorageMessage] = Behaviors.receive { (context, message) =>
    message match {
      case StorageMessage.SaveMessage(items) =>
        context.log.info(s"Receive new items, count: ${items.size}")
        val newItems = items.filterNot(item => sendIds(item.id))
        if (newItems.nonEmpty) {
          sender ! SenderMessage.SendNews(newItems)
        }
        storage(sendIds ++ items.map(_.id), sender)
    }
  }
}
