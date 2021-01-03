package io.github.kirill5k.telegrambot.clients

import cats.effect.Sync
import cats.implicits._
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import io.github.kirill5k.telegrambot.common.config.TelegramConfig
import io.github.kirill5k.telegrambot.common.errors.AppError
import io.circe.generic.auto._
import sttp.client._
import sttp.client.circe._

final case class ChatId(value: Long) extends AnyVal

final case class Chat(id: ChatId)
final case class Message(message_id: Long, chat: Chat, text: Option[String])
final case class Update(update_id: Long, message: Option[Message])

final case class UpdateResponse[T](ok: Boolean, result: T)

trait TelegramClient[F[_]] {
  def send(chatId: ChatId, text: String): F[Unit]
  def pollUpdates: Stream[F, Update]
}

final private class LiveTelegramClient[F[_]](
    private val config: TelegramConfig
)(implicit
    val B: SttpBackend[F, Nothing, NothingT],
    val F: Sync[F],
    val L: Logger[F]
) extends TelegramClient[F] {

  override def send(chatId: ChatId, text: String): F[Unit] =
    basicRequest
      .get(uri"${config.baseUri}/bot${config.botKey}/sendMessage?chat_id=${chatId.value}&text=$text")
      .send()
      .flatMap { r =>
        r.body match {
          case Right(_) => F.unit
          case Left(error) =>
            L.error(s"error sending message to telegram: ${r.code}\n$error") *>
              F.raiseError(AppError.Http(r.code.code, s"error sending message to telegram chat id ${chatId.value}: ${r.code}"))
        }
      }

  override def pollUpdates: Stream[F, Update] =
    Stream
      .unfoldLoopEval(0L) { o =>
        getUpdates(o).map { updates =>
          (updates, updates.map(_.update_id).maxOption.orElse(Some(o)))
        }
      }
      .flatMap(Stream.emits)

  private def getUpdates(offset: Long): F[List[Update]] =
    basicRequest
      .get(uri"${config.baseUri}/bot${config.botKey}/getUpdates?offset=${offset + 1}&timeout=0.5&allowed_updates=[message]")
      .response(asJson[UpdateResponse[List[Update]]])
      .send()
      .flatMap { r =>
        r.body match {
          case Right(res) => res.result.pure[F]
          case Left(error) =>
            L.error(s"error getting updates from telegram: ${r.code}\n$error") *>
              F.raiseError(AppError.Http(r.code.code, s"error getting updates from telegram: ${error.getMessage}"))
        }
      }
}

object TelegramClient {

  def make[F[_]: Sync: Logger](
      config: TelegramConfig,
      backend: SttpBackend[F, Nothing, NothingT]
  ): F[TelegramClient[F]] =
    Sync[F].delay(new LiveTelegramClient[F](config)(B = backend, F = Sync[F], L = Logger[F]))
}
