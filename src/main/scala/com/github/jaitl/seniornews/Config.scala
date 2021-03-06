package com.github.jaitl.seniornews

import scala.concurrent.duration.FiniteDuration

case class Config(
    credentials: TokenConfig,
    chanelSchedule: ChanelSchedulerConfig,
    subscriberStorage: SubscriberStorageConfig,
    newsStorage: NewsStorageConfig
)

case class TokenConfig(token: String)

case class ChanelSchedulerConfig(interval: FiniteDuration)

case class SubscriberStorageConfig(dbPath: String)

case class NewsStorageConfig(dbPath: String, storeCount: Int)
