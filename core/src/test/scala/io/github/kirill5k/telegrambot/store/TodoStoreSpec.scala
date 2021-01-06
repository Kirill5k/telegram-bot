package io.github.kirill5k.telegrambot.store

import cats.effect.IO
import io.github.kirill5k.telegrambot.CatsSpec
import io.github.kirill5k.telegrambot.clients.ChatId

class TodoStoreSpec extends CatsSpec {

  "An InMemoryTodoStore" should {

    "add" should {
      "add todo items to the store" in {
        val res = for {
          store <- TodoStore.inMemory[IO]
          _ <- store.addItem(ChatId(1L), TodoItem("homework"))
          _ <- store.addItem(ChatId(1L), TodoItem("exercise"))
          items <- store.getItems(ChatId(1L))
        } yield items

        res.unsafeToFuture().map(_ mustBe List(TodoItem("homework"), TodoItem("exercise")))
      }
    }

    "clearAll" should {
      "clear all todo items from the store" in {
        val res = for {
          store <- TodoStore.inMemory[IO]
          _ <- store.addItem(ChatId(1L), TodoItem("homework"))
          _ <- store.addItem(ChatId(1L), TodoItem("exercise"))
          _ <- store.clearAll(ChatId(1L))
          items <- store.getItems(ChatId(1L))
        } yield items

        res.unsafeToFuture().map(_ mustBe Nil)
      }
    }

    "clearOne" should {
      "clear 1 todo item from the store" in {
        val res = for {
          store <- TodoStore.inMemory[IO]
          _ <- store.addItem(ChatId(1L), TodoItem("homework"))
          _ <- store.addItem(ChatId(1L), TodoItem("rest"))
          _ <- store.addItem(ChatId(1L), TodoItem("exercise"))
          _ <- store.clearOne(ChatId(1L), 1)
          items <- store.getItems(ChatId(1L))
        } yield items

        res.unsafeToFuture().map(_ mustBe List(TodoItem("homework"), TodoItem("exercise")))
      }

      "handle invalid indices" in {
        val res = for {
          store <- TodoStore.inMemory[IO]
          _ <- store.clearOne(ChatId(1L), 2)
          items <- store.getItems(ChatId(1L))
        } yield items

        res.unsafeToFuture().map(_ mustBe Nil)
      }
    }
  }
}
