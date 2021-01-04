package io.github.kirill5k.telegrambot.store

import cats.effect.IO
import io.github.kirill5k.telegrambot.CatsSpec
import io.github.kirill5k.telegrambot.clients.Username

class TodoStoreSpec extends CatsSpec {

  "An InMemoryTodoStore" should {

    "add todo items to the store" in {
      val res = for {
        store <- TodoStore.inMemory[IO]
        _ <- store.addItem(Username("u1"), TodoItem("homework"))
        _ <- store.addItem(Username("u1"), TodoItem("exercise"))
        items <- store.getItems(Username("u1"))
      } yield items

      res.unsafeToFuture().map(_ mustBe List(TodoItem("exercise"), TodoItem("homework")))
    }

    "clear all todo items from the store" in {
      val res = for {
        store <- TodoStore.inMemory[IO]
        _ <- store.addItem(Username("u1"), TodoItem("homework"))
        _ <- store.addItem(Username("u1"), TodoItem("exercise"))
        _ <- store.clear(Username("u1"))
        items <- store.getItems(Username("u1"))
      } yield items

      res.unsafeToFuture().map(_ mustBe Nil)
    }
  }
}
