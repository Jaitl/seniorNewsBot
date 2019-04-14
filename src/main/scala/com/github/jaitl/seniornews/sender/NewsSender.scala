package com.github.jaitl.seniornews.sender

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.github.jaitl.seniornews.models.NewsItem

object NewsSender {
  sealed trait SenderMessage

  object SenderMessage {
    case class SendNews(items: Seq[NewsItem]) extends SenderMessage
  }

  def name: String = "NewsSender"

  def sender: Behavior[SenderMessage] = Behaviors.receive { (context, message) =>
    message match {
      case SenderMessage.SendNews(items) =>
        items.foreach(i => context.log.info(i.title))
    }
    Behaviors.same
  }
}
