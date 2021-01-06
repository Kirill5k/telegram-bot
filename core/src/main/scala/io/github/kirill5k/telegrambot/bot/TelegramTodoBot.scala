package io.github.kirill5k.telegrambot.bot

import cats.Monad
import cats.effect.Sync
import cats.implicits._
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import io.github.kirill5k.telegrambot.clients.TelegramBotClient
import io.github.kirill5k.telegrambot.store.TodoStore

trait TelegramTodoBot[F[_]] {
  def run: Stream[F, Unit]
}

final private class LiveTelegramTodoBot[F[_]: Monad: Logger](
    private val telegramBotClient: TelegramBotClient[F],
    private val todoStore: TodoStore[F]
) extends TelegramTodoBot[F] {

  private def pollCommands: Stream[F, BotCommand] =
    telegramBotClient.pollUpdates
      .evalTap(m => Logger[F].info(m.toString))
      .map(_.message)
      .unNone
      .filter(!_.from.is_bot)
      .map(BotCommand.from)
      .unNone

  private def processCommand(command: BotCommand): F[Unit] = command match {
    case c @ BotCommand.Add(chatId, todo) =>
      todoStore.addItem(chatId, todo) *> telegramBotClient.send(chatId, c.response)
    case c @ BotCommand.Clear(chatId) =>
      todoStore.clear(chatId) *> telegramBotClient.send(chatId, c.response)
    case c @ BotCommand.Show(chatId) =>
      todoStore
        .getItems(chatId)
        .map(_.zipWithIndex.map { case (t, i) => s"\t${i + 1}: ${t.todo}" })
        .map(items => (c.response :: items).mkString("\n"))
        .flatMap(items => telegramBotClient.send(chatId, items))
    case c @ BotCommand.Help(chatId) =>
      telegramBotClient.send(chatId, c.response)
    case c =>
      telegramBotClient.send(c.chatId, c.response)
  }

  override def run: Stream[F, Unit] =
    pollCommands
      .evalMap(processCommand)
      .handleErrorWith { error =>
        Stream.eval_(Logger[F].error(error)("Error during message processing")) ++ run
      }
}

object TelegramTodoBot {
  def make[F[_]: Sync: Logger](
      telegramBotClient: TelegramBotClient[F],
      todoStore: TodoStore[F]
  ): F[TelegramTodoBot[F]] =
    Sync[F].delay(new LiveTelegramTodoBot[F](telegramBotClient, todoStore))
}
