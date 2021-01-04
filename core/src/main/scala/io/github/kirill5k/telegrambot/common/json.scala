package io.github.kirill5k.telegrambot.common

import cats.Applicative
import cats.effect.Sync
import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto._
import io.github.kirill5k.telegrambot.clients.{ChatId, Username}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

object json extends JsonCodecs

trait JsonCodecs {
  implicit def deriveEntityEncoder[F[_]: Applicative, A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]
  implicit def deriveEntityDecoder[F[_]: Sync, A: Decoder]: EntityDecoder[F, A]        = jsonOf[F, A]

  implicit val usEncoder: Encoder[Username] = deriveUnwrappedEncoder
  implicit val usDecoder: Decoder[Username] = deriveUnwrappedDecoder

  implicit val cidEncoder: Encoder[ChatId] = deriveUnwrappedEncoder
  implicit val cidDecoder: Decoder[ChatId] = deriveUnwrappedDecoder
}
