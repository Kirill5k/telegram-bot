package io.github.kirill5k.telegrambot.common

import cats.effect.{Blocker, ContextShift, Sync}
import io.github.kirill5k.telegrambot.clients.ChatId
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._
import pureconfig.ConfigSource

object config {

  final case class ServerConfig(
      host: String,
      port: Int
  )

  final case class TelegramConfig(
      baseUri: String,
      botKey: String
  )

  final case class AppConfig(
      server: ServerConfig,
      telegram: TelegramConfig
  )

  object AppConfig {
    def load[F[_]: Sync: ContextShift](blocker: Blocker): F[AppConfig] =
      ConfigSource.default.loadF[F, AppConfig](blocker)
  }
}
