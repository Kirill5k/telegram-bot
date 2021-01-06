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
  def addItem(cid: ChatId, todo: TodoItem): F[Unit]
  def getItems(cid: ChatId): F[List[TodoItem]]
  def clearOne(cid: ChatId, index: Int): F[Unit]
  def clearAll(cid: ChatId): F[Unit]
}

final private class InMemoryTodoStore[F[_]: Functor](
    private val store: Ref[F, Map[ChatId, List[TodoItem]]]
) extends TodoStore[F] {

  def addItem(cid: ChatId, todo: TodoItem): F[Unit] =
    store.update(items => items.updated(cid, items.getOrElse(cid, Nil) :+ todo))

  def getItems(cid: ChatId): F[List[TodoItem]] =
    store.get.map(_.getOrElse(cid, Nil))

  def clearAll(cid: ChatId): F[Unit] =
    store.update(_.removed(cid))

  override def clearOne(cid: ChatId, index: Int): F[Unit] =
    store.update{ items =>
      val ts = items.getOrElse(cid, Nil)
      val res = if (ts.size < index) ts else {
        val (left, right) = ts.splitAt(index)
        left ::: right.tail
      }
      items.updated(cid, res)
    }
}

object TodoStore {

  def inMemory[F[_]: Sync]: F[TodoStore[F]] =
    Ref.of(Map.empty[ChatId, List[TodoItem]])
      .map(store => new InMemoryTodoStore[F](store))
}
