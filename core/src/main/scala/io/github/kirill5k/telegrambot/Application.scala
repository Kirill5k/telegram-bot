package io.github.kirill5k.telegrambot

import cats.effect.{ExitCode, IO, IOApp}
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.github.kirill5k.telegrambot.bot.TelegramTodoBot
import io.github.kirill5k.telegrambot.clients.TelegramBotClient
import io.github.kirill5k.telegrambot.common.config.AppConfig
import io.github.kirill5k.telegrambot.store.TodoStore

object Application extends IOApp {

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] = {
    Resources.make[IO].use { res =>
      for {
        config <- AppConfig.load[IO](res.blocker) <* logger.info("loaded config")
        client <- TelegramBotClient.make[IO](config.telegram, res.httpClientBackend)
        store <- TodoStore.inMemory[IO]
        bot <- TelegramTodoBot.make(client, store)
        _ <- bot.run.compile.drain
      } yield ExitCode.Success
    }
  }
}
