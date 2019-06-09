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
import info.mukel.telegrambot4s.api.TelegramApiException
import info.mukel.telegrambot4s.api.TelegramBot
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.methods.SendMessage
import info.mukel.telegrambot4s.models.ChatId.Chat

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

class TelegramBotActor(
    val token: String,
    subscriberStorage: SubscriberStorage
) extends AbstractBehavior[TelegramMessages]
    with TelegramBot
    with Polling
    with Commands {

  import TelegramBotActor._

  onCommand("/start") { implicit msg =>
    logger.info(s"New subscriber: ${msg.chat.id}")
    subscriberStorage.addSubscriber(msg.chat.id)
    reply("You subscribed to Senior News!")
  }

  override def onMessage(msg: TelegramMessages): Behavior[TelegramMessages] = msg match {
    case StartListenMessages =>
      this.run()
      logger.info("Start telegram bot")
      Behaviors.same
    case SendNews(items, replyTo) if subscriberStorage.subscribers().nonEmpty =>
      val messages = for {
        sub <- subscriberStorage.subscribers()
        itm <- items
      } yield SendNewsTo(sub, itm)
      sendMessages(messages, replyTo)
      Behaviors.same
    case SendNews(_, _) =>
      logger.info("no subscribers")
      Behaviors.same
  }

  private def sendMessages(items: Set[SendNewsTo], replyTo: ActorRef[StorageMessage]): Unit = {
    val resultFuture = Source(items.to[scala.collection.immutable.Seq])
      .mapAsync(1) { msg =>
        send(msg)
          .recover {
            case ex: TelegramApiException if ex.errorCode == 403 =>
              if (subscriberStorage.subscribers().contains(msg.subscriber)) {
                subscriberStorage.removeSubscriber(msg.subscriber)
              }
            case ex: TelegramApiException =>
              logger.error("Fail during send message", ex)
          }
      }
      .runWith(Sink.seq)

    resultFuture.onComplete {
      case Success(_) =>
        val sendIds: Set[String] = items.map(_.item.id)
        replyTo ! StorageMessage.SendNews(items.map(_.item.id))
        logger.debug(s"Messages success send, count: ${sendIds.size}")
      case Failure(ex) =>
        logger.error("Error during send messages", ex)
    }
  }

  private def send(msg: SendNewsTo): Future[Unit] = {
    request(SendMessage(Chat(msg.subscriber), s"${msg.item.title}: ${msg.item.url}")).map(_ => Unit)
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
