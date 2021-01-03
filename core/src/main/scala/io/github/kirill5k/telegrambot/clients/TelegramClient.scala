package io.github.kirill5k.telegrambot.clients

import fs2.Stream

final case class Offset(value: Long) extends AnyVal
final case class ChatId(value: Long) extends AnyVal

final case class Chat(id: ChatId)
final case class Message(message_id: Long, chat: Chat, text: Option[String])
final case class Update(update_id: Long, message: Option[Message])

trait TelegramClient[F[_]] {
  def send(chatId: ChatId, text: String): F[Unit]
  def pollUpdates(offset: Offset): Stream[F, Update]
}
