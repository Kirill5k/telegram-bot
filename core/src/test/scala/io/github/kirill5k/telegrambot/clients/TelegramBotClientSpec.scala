package io.github.kirill5k.telegrambot.clients

import cats.effect.IO
import io.github.kirill5k.telegrambot.SttpClientSpec
import io.github.kirill5k.telegrambot.common.config.TelegramConfig
import io.github.kirill5k.telegrambot.common.errors.AppError
import sttp.client.{NothingT, Response, SttpBackend}
import sttp.model.{Method, StatusCode}
import io.github.kirill5k.telegrambot.TestUtil._

class TelegramBotClientSpec extends SttpClientSpec {

  val message = "lorem ipsum dolor sit amet"
  val config = TelegramConfig("http://telegram.com", "BOT-KEY")

  "TelegramClient" should {

    "send message to a chat" in {
      val testingBackend: SttpBackend[IO, Nothing, NothingT] = backendStub
        .whenRequestMatchesPartial {
          case r if isGoingTo(r, Method.GET, "telegram.com", List("botBOT-KEY", "sendMessage"), Map("chat_id" -> "42", "text" -> message)) =>
            Response.ok("success")
          case _ => throw new RuntimeException()
        }

      val telegramClient = TelegramBotClient.make(config, testingBackend)

      val result = telegramClient.flatMap(_.send(ChatId(42), message))

      result.unsafeToFuture().map(_ must be(()))
    }

    "return error when not success" in {
      val testingBackend: SttpBackend[IO, Nothing, NothingT] = backendStub
        .whenRequestMatchesPartial {
          case r if isGoingTo(r, Method.GET, "telegram.com", List("botBOT-KEY", "sendMessage"), Map("chat_id" -> "42", "text" -> message)) =>
            Response("fail", StatusCode.BadRequest)
          case _ => throw new RuntimeException()
        }

      val telegramClient = TelegramBotClient.make(config, testingBackend)

      val result = telegramClient.flatMap(_.send(ChatId(42), message))

      result.attempt.unsafeToFuture().map(_ must be(Left(AppError.Http(400, "error sending message to telegram chat id 42: 400"))))
    }

    "poll updates continuously" in {
      val testingBackend: SttpBackend[IO, Nothing, NothingT] = backendStub
        .whenRequestMatchesPartial {
          case r if isGoingTo(r, Method.GET, "telegram.com", List("botBOT-KEY", "getUpdates"), Map("offset" -> "0", "timeout" -> "0.5", "allowed_updates" -> "[message]")) =>
            Response.ok(readFile("telegram-bot-updates-1.json"))
          case r if isGoingTo(r, Method.GET, "telegram.com", List("botBOT-KEY", "getUpdates"), Map("offset" -> "2", "timeout" -> "0.5", "allowed_updates" -> "[message]")) =>
            Response.ok(readFile("telegram-bot-updates-2.json"))
          case _ => throw new RuntimeException()
        }

      val result = for {
        client <- TelegramBotClient.make(config, testingBackend)
        updates <- client.pollUpdates.take(3).compile.toList
      } yield updates

      result.unsafeToFuture().map { upd =>
        upd must have size 3
      }
    }
  }
}
