package io.github.kirill5k.telegrambot

import cats.effect.{Blocker, Concurrent, ContextShift, Resource}
import cats.implicits._
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.{NothingT, SttpBackend}

final case class Resources[F[_]](
    blocker: Blocker,
    httpClientBackend: SttpBackend[F, Nothing, NothingT]
)

object Resources {

  private def httpClientBackend[F[_]: Concurrent: ContextShift]: Resource[F, SttpBackend[F, Nothing, NothingT]] =
    Resource.make(AsyncHttpClientCatsBackend[F]())(_.close())

  def make[F[_]: Concurrent: ContextShift]: Resource[F, Resources[F]] =
    (Blocker[F], httpClientBackend[F]).mapN(Resources.apply[F])
}
