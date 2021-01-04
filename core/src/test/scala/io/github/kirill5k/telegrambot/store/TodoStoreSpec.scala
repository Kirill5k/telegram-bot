package io.github.kirill5k.telegrambot.store

import cats.effect.IO
import io.github.kirill5k.telegrambot.CatsSpec
import io.github.kirill5k.telegrambot.clients.Username

class TodoStoreSpec extends CatsSpec {

  "An InMemoryTodoStore" should {

    "add todo items to the store" in {
      val res = for {
        store <- TodoStore.inMemory[IO]
        _ <- store.addItem(Username("u1"), "homework")
        _ <- store.addItem(Username("u1"), "exercise")
        items <- store.getItems(Username("u1"))
      } yield items

      res.unsafeToFuture().map(_ mustBe List(TodoItem(0, "homework"), TodoItem(1, "exercise")))
    }

    "clear all todo items from the store" in {
      val res = for {
        store <- TodoStore.inMemory[IO]
        _ <- store.addItem(Username("u1"), "homework")
        _ <- store.addItem(Username("u1"), "exercise")
        _ <- store.clear(Username("u1"))
        items <- store.getItems(Username("u1"))
      } yield items

      res.unsafeToFuture().map(_ mustBe Nil)
    }
  }
}
