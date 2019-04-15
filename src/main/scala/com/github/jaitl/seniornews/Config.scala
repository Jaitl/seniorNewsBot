package com.github.jaitl.seniornews

import scala.concurrent.duration.FiniteDuration

case class Config(credentials: TokenConfig, chanelSchedule: ChanelSchedulerConfig)

case class TokenConfig(token: String)

case class ChanelSchedulerConfig(interval: FiniteDuration)
