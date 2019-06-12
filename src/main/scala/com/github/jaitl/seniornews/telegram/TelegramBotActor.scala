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
    subscriberStorage.addSubscriber(msg.chat.id) match {
      case Success(_) =>
        logger.info(s"New subscriber, id: ${msg.chat.id}")
        reply("You subscribed to Senior News!")
      case Failure(ex) =>
        logger.error("Fail to store new subscriber", ex)
    }
  }

  override def onMessage(msg: TelegramMessages): Behavior[TelegramMessages] = msg match {
    case StartListenMessages =>
      this.run()
      logger.info(s"Start telegram bot, count subscribers: ${subscriberStorage.subscribers().size}")
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
          .recover(handleSendErrors(msg))
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

  private def send(msg: SendNewsTo): Future[Unit] =
    request(SendMessage(Chat(msg.subscriber), s"${msg.item.title.trim}: ${msg.item.url}")).map(_ => Unit)

  private def handleSendErrors(msg: SendNewsTo): PartialFunction[Throwable, Unit] = {
    case ex: TelegramApiException if ex.errorCode == 403 =>
      if (subscriberStorage.subscribers().contains(msg.subscriber)) {
        subscriberStorage.removeSubscriber(msg.subscriber) match {
          case Success(_)   => logger.info(s"Subscriber removed, id: ${msg.subscriber}")
          case Failure(exx) => logger.error("Fail to remove subscriber", exx)
        }
      }
    case ex: TelegramApiException =>
      logger.error("Fail during send message", ex)
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
