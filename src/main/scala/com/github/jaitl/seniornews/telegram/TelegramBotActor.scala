package com.github.jaitl.seniornews.telegram

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.github.jaitl.seniornews.models.NewsItem
import com.github.jaitl.seniornews.storage.NewsStorageActor.StorageMessage
import com.github.jaitl.seniornews.telegram.TelegramBotActor.TelegramMessages
import com.github.jaitl.seniornews.telegram.TelegramBotActor.TelegramMessages.SendNews
import com.github.jaitl.seniornews.telegram.TelegramBotActor.TelegramMessages.StartListenMessages
import info.mukel.telegrambot4s.api.Polling
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.methods.SendMessage
import info.mukel.telegrambot4s.models.ChatId.Chat
import info.mukel.telegrambot4s.models.Message

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

class TelegramBotActor(
    val token: String
) extends AbstractBehavior[TelegramMessages]
    with TelegramBot
    with Polling
    with Commands {

  import TelegramBotActor._

  private val subscribers: mutable.Set[Long] = mutable.Set.empty

  onCommand("/start") { implicit msg =>
    logger.info(s"New subscriber: ${msg.chat.id}")
    subscribers += msg.chat.id
    reply("You subscribed to Senior News!")
  }

  override def onMessage(msg: TelegramMessages): Behavior[TelegramMessages] = msg match {
    case StartListenMessages =>
      this.run()
      logger.info("Start telegram bot")
      Behaviors.same
    case SendNews(items, replyTo) if subscribers.nonEmpty =>
      val messages = for {
        sub <- subscribers
        itm <- items
      } yield SendNewsTo(sub, itm)
      sendMessages(messages.toSet, replyTo)
      Behaviors.same
    case SendNews(_, _) =>
      logger.info("no subscribers")
      Behaviors.same
  }

  private def sendMessages(items: Set[SendNewsTo], replyTo: ActorRef[StorageMessage]): Unit = {
    val resultFuture = Source(items.to[scala.collection.immutable.Seq])
      .mapAsync(1) { msg =>
        send(msg)
      }
      .runWith(Sink.seq)

    resultFuture.onComplete {
      case Success(_) =>
        val sendIds: Set[String] = items.map(_.item.id)
        replyTo ! StorageMessage.SendNews(sendIds)
        logger.debug(s"Messages success send, count: ${sendIds.size}")
      case Failure(ex) =>
        logger.error("Error during send messages", ex)
    }
  }

  private def send(msg: SendNewsTo): Future[Message] = {
    request(SendMessage(Chat(msg.subscriber), s"${msg.item.title}: ${msg.item.url}"))
  }
}

object TelegramBotActor {
  def name: String = "BotActor"

  case class SendNewsTo(subscriber: Long, item: NewsItem)

  sealed trait TelegramMessages

  object TelegramMessages {
    object StartListenMessages extends TelegramMessages
    case class SendNews(items: Seq[NewsItem], replyTo: ActorRef[StorageMessage]) extends TelegramMessages
  }
}
