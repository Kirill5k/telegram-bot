package io.github.kirill5k.telegrambot.bot

import io.github.kirill5k.telegrambot.clients.{ChatId, Update, Username}

import scala.util.Random

sealed trait BotCommand {
  def chatId: ChatId
  def response: String
}

object BotCommand {
  final case class Add(chatId: ChatId, username: Username, todo: String) extends BotCommand {
    override def response: String = {
      val res = Random.shuffle(List("Ok", "Sure", "Noted", "Certainly")).head
      s"$res, ${username.value}. I have updated your todo-list"
    }
  }

  final case class Clear(chatId: ChatId, username: Username) extends BotCommand {
    val response: String = "Your todo-list was cleared!"
  }

  final case class Show(chatId: ChatId, username: Username) extends BotCommand {
    override def response: String = s"Sure thing, ${username.value}. Here is your current todo-list:"
  }

  final case class Help(chatId: ChatId) extends BotCommand {
    val response: String =
      """
        |This bot manages your todo-list. Just write a command and the bot will respond to it! Commands:
        |"/list" - view your current todo-list
        |"/clear" - clear your current todo-list
        |"/todo <todo-item>" - add a <todo-item> to your list
        |""".stripMargin
  }

  final case class Unknown(chatId: ChatId) extends BotCommand {
    val response: String = """Unrecognized command. type "/help" to see of available commands"""
  }
}
