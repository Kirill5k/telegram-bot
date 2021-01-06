package io.github.kirill5k.telegrambot.store

import cats.Functor
import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._
import io.github.kirill5k.telegrambot.clients.ChatId

final case class TodoItem(
    todo: String
)

trait TodoStore[F[_]] {
  def addItem(pid: ChatId, todo: TodoItem): F[Unit]
  def getItems(pid: ChatId): F[List[TodoItem]]
  def clear(pid: ChatId): F[Unit]
}

final private class InMemoryTodoStore[F[_]: Functor](
    private val store: Ref[F, Map[ChatId, List[TodoItem]]]
) extends TodoStore[F] {

  def addItem(pid: ChatId, todo: TodoItem): F[Unit] =
    store.update(items => items.updated(pid, items.getOrElse(pid, Nil) :+ todo))

  def getItems(pid: ChatId): F[List[TodoItem]] =
    store.get.map(_.getOrElse(pid, Nil))

  def clear(pid: ChatId): F[Unit] =
    store.update(_.removed(pid))
}

object TodoStore {

  def inMemory[F[_]: Sync]: F[TodoStore[F]] =
    Ref.of(Map.empty[ChatId, List[TodoItem]])
      .map(store => new InMemoryTodoStore[F](store))
}
