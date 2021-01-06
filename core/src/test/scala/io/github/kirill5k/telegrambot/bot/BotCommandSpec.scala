package io.github.kirill5k.telegrambot.bot

import io.github.kirill5k.telegrambot.clients.{Chat, ChatId, Message, MessageOrigin, Username}
import io.github.kirill5k.telegrambot.store.TodoItem
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BotCommandSpec extends AnyWordSpec with Matchers {

  "A BotCommand" should {

    "return unknown when invalid command supplied" in {
      val msg = message(Some("/foo bar"))

      BotCommand.from(msg) mustBe Some(BotCommand.Unknown(ChatId(42), "/foo"))
    }

    "return none when no command provided" in {
      val msg = message(Some("hello, world"))

      BotCommand.from(msg) mustBe None
    }

    "create Add command from message" in {
      val msg = message(Some("/todo clean room"))

      BotCommand.from(msg) mustBe Some(BotCommand.Add(ChatId(42), TodoItem("clean room")))
    }

    "return error when can't add new item" in {
      val msg = message(Some("/todo "))

      BotCommand.from(msg) mustBe Some(BotCommand.Error(ChatId(42), "Unable to add new todo item. Missing the actual item. Use \"/help\" for more information"))
    }
  }

  def message(text: Option[String]): Message =
    Message(
      1L,
      Chat(ChatId(42L)),
      text,
      Some(MessageOrigin(Some(Username("u1")), Some("user-1"), false, 2L))
    )
}
