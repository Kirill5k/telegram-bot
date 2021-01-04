package io.github.kirill5k.telegrambot.bot

import cats.Monad
import cats.effect.Sync
import cats.implicits._
import fs2.Stream
import io.github.kirill5k.telegrambot.clients.TelegramBotClient
import io.github.kirill5k.telegrambot.store.TodoStore

trait TelegramTodoBot[F[_]] {
  def run: Stream[F, Unit]
}

final private class LiveTelegramTodoBot[F[_]: Monad](
    private val telegramBotClient: TelegramBotClient[F],
    private val todoStore: TodoStore[F]
) extends TelegramTodoBot[F] {

  private def pollCommands: Stream[F, BotCommand] =
    telegramBotClient.pollUpdates
      .map(_.message)
      .unNone
      .filter(!_.from.is_bot)
      .map(BotCommand.from)
      .unNone

  private def processCommand(command: BotCommand): F[Unit] = command match {
    case c @ BotCommand.Add(chatId, username, todo) =>
      todoStore.addItem(username, todo) *> telegramBotClient.send(chatId, c.response)
    case c @ BotCommand.Clear(chatId, username) =>
      todoStore.clear(username) *> telegramBotClient.send(chatId, c.response)
    case c @ BotCommand.Show(chatId, username) =>
      todoStore
        .getItems(username)
        .map(_.map(i => s"\t$i"))
        .map(items => (c.response :: items).mkString("\n"))
        .flatMap(items => telegramBotClient.send(chatId, items))
    case c @ BotCommand.Help(chatId) =>
      telegramBotClient.send(chatId, c.response)
    case c @ BotCommand.Unknown(chatId, _) =>
      telegramBotClient.send(chatId, c.response)
  }

  override def run: Stream[F, Unit] =
    pollCommands.evalMap(processCommand)
}

object TelegramTodoBot {
  def make[F[_]: Sync](
      telegramBotClient: TelegramBotClient[F],
      todoStore: TodoStore[F]
  ): F[TelegramTodoBot[F]] =
    Sync[F].delay(new LiveTelegramTodoBot[F](telegramBotClient, todoStore))
}
