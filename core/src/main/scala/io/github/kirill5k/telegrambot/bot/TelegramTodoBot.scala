package io.github.kirill5k.telegrambot.bot

import fs2.Stream
import io.github.kirill5k.telegrambot.clients.TelegramBotClient
import io.github.kirill5k.telegrambot.store.TodoStore




trait TelegramTodoBot[F[_]] {
  def run: Stream[F, Unit]
}

final private class LiveTelegramTodoBot[F[_]](
    private val telegramBotClient: TelegramBotClient[F],
    private val todoStore: TodoStore[F]
) extends TelegramTodoBot[F] {

  override def run: Stream[F, Unit] = ???
}
