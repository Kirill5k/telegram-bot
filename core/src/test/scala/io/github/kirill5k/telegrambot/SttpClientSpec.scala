package io.github.kirill5k.telegrambot

import cats.effect.IO
import sttp.client
import sttp.client.asynchttpclient.WebSocketHandler
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub
import sttp.model.Method

trait SttpClientSpec extends CatsSpec {

  def backendStub: SttpBackendStub[IO, Nothing, WebSocketHandler] =
    AsyncHttpClientCatsBackend.stub[IO]

  def isGoingTo(
      req: client.Request[_, _],
      method: Method,
      host: String,
      paths: Seq[String] = Nil,
      params: Map[String, String] = Map.empty
  ): Boolean =
    req.uri.host == host &&
      (paths.isEmpty || req.uri.path == paths) &&
      req.method == method &&
      req.uri.params.toMap.toSet[(String, String)].subsetOf(params.toSet)
}
