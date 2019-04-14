package com.github.jaitl.seniornews.aggregator

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.github.jaitl.seniornews.aggregator.ChannelActor.ChannelMessage

import scala.concurrent.duration.FiniteDuration

object ChannelScheduler {
  sealed trait SchedulerMessage
  case object TickMessage extends SchedulerMessage

  def name: String = "ChannelScheduler"

  def scheduler(
      config: ChanelSchedulerConfig,
      channels: Seq[ActorRef[ChannelMessage]]
  ): Behavior[SchedulerMessage] = Behaviors.withTimers { timers =>
    timers.startPeriodicTimer(TickMessage, TickMessage, config.interval)
    Behaviors.receiveMessagePartial {
      case TickMessage =>
        channels.foreach(chanel => chanel ! ChannelMessage.ScheduleMessage)
        Behaviors.same
    }
  }
}

case class ChanelSchedulerConfig(interval: FiniteDuration)
