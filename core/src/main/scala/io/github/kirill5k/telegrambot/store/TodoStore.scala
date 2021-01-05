package io.github.kirill5k.telegrambot.store

import cats.Functor
import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._
import io.github.kirill5k.telegrambot.clients.Username

final case class TodoItem(
    todo: String
)

trait TodoStore[F[_]] {
  def addItem(username: Username, todo: String): F[Unit]
  def getItems(username: Username): F[List[TodoItem]]
  def clear(username: Username): F[Unit]
}

final private class InMemoryTodoStore[F[_]: Functor](
    private val store: Ref[F, Map[Username, List[TodoItem]]]
) extends TodoStore[F] {

  def addItem(username: Username, todo: String): F[Unit] =
    store.update(items => items.updated(username, items.getOrElse(username, Nil) :+ TodoItem(todo)))

  def getItems(username: Username): F[List[TodoItem]] =
    store.get.map(_.getOrElse(username, Nil))

  def clear(username: Username): F[Unit] =
    store.update(_.removed(username))
}

object TodoStore {

  def inMemory[F[_]: Sync]: F[TodoStore[F]] =
    Ref.of(Map.empty[Username, List[TodoItem]])
      .map(store => new InMemoryTodoStore[F](store))
}
