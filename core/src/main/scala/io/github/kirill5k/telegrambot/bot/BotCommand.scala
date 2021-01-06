package io.github.kirill5k.telegrambot.bot

import io.github.kirill5k.telegrambot.clients.{ChatId, Message}
import io.github.kirill5k.telegrambot.store.TodoItem

import scala.util.Random

sealed trait BotCommand {
  def chatId: ChatId
  def response: String
}

object BotCommand {
  final case class Add(chatId: ChatId, todo: TodoItem) extends BotCommand {
    override val response: String = {
      val res = Random.shuffle(List("Ok", "Sure", "Noted", "Certainly")).head
      s"$res. I have updated your todo-list"
    }
  }

  final case class Clear(chatId: ChatId) extends BotCommand {
    override val response: String = "Your todo-list was cleared!"
  }

  final case class Show(chatId: ChatId) extends BotCommand {
    override val response: String = s"Sure thing. Here is your current todo-list:"
  }

  final case class Help(chatId: ChatId) extends BotCommand {
    override val response: String =
      """
        |This bot manages your todo-list. Just write a command and the bot will respond to it! Commands:
        |"/show" - view your current todo-list
        |"/clear" - clear your current todo-list
        |"/todo <todo-item>" - add a <todo-item> to your list
        |""".stripMargin
  }

  final case class Error(chatId: ChatId, error: String) extends BotCommand {
    override def response: String = error
  }

  final case class Unknown(chatId: ChatId, command: String) extends BotCommand {
    override val response: String = s"""Unrecognized command "$command". type "/help" to see all available commands"""
  }

  private val TodoRegex = "/todo ([\\w\\d]+)".r

  def from(message: Message): Option[BotCommand] =
    message.text.filter(_.startsWith("/")).map {
      case c if c.startsWith("/show") | c.startsWith("/start") =>
        Show(message.chat.id)
      case c if c.startsWith("/clear") =>
        Clear(message.chat.id)
      case c if c.startsWith("/help") =>
        Help(message.chat.id)
      case c if c.startsWith("/todo") =>
        c match {
          case TodoRegex(todo) => Add(message.chat.id, TodoItem(todo))
          case _               => Error(message.chat.id, """Unable to add new todo item. Missing the actual item. Use "/help" for more information""")
        }
      case c =>
        Unknown(message.chat.id, c.split(" ").head)
    }
}
