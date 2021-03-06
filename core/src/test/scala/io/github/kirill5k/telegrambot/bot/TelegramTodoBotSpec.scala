package io.github.kirill5k.telegrambot.bot

import cats.effect.IO
import fs2.Stream
import io.github.kirill5k.telegrambot.CatsSpec
import io.github.kirill5k.telegrambot.clients._
import io.github.kirill5k.telegrambot.store.{TodoItem, TodoStore}

class TelegramTodoBotSpec extends CatsSpec {

  "A TelegramTodoBot" should {

    "show current todos" in {
      val (client, store) = mocks

      when(client.pollUpdates).thenReturn(Stream.emit(update(Some("/show"))))
      when(store.getItems(any[ChatId])).thenReturn(IO.pure(List(TodoItem("homework"), TodoItem("exercise"))))
      when(client.send(any[ChatId], any[String])).thenReturn(IO.unit)

      val res = for {
        bot <- TelegramTodoBot.make(client, store)
        _   <- bot.run.take(1).compile.drain
      } yield ()

      res.unsafeToFuture().map { r =>
        verify(client).pollUpdates
        verify(store).getItems(ChatId(42L))
        verify(client).send(ChatId(42), "Sure thing. Here is your current todo-list:\n\t1: homework\n\t2: exercise")
        r mustBe ()
      }
    }

    "add new todos" in {
      val (client, store) = mocks

      when(client.pollUpdates).thenReturn(Stream.emit(update(Some("/todo homework"))))
      when(store.addItem(any[ChatId], any[TodoItem])).thenReturn(IO.unit)
      when(client.send(any[ChatId], any[String])).thenReturn(IO.unit)

      val res = for {
        bot <- TelegramTodoBot.make(client, store)
        _   <- bot.run.take(1).compile.drain
      } yield ()

      res.unsafeToFuture().map { r =>
        verify(client).pollUpdates
        verify(store).addItem(ChatId(42L), TodoItem("homework"))
        verify(client).send(eqTo(ChatId(42)), endsWith("I have updated your todo-list"))
        r mustBe ()
      }
    }

    "handle invalid syntax when adding new todos" in {
      val (client, store) = mocks

      when(client.pollUpdates).thenReturn(Stream.emit(update(Some("/todo-foo-bar"))))
      when(client.send(any[ChatId], any[String])).thenReturn(IO.unit)

      val res = for {
        bot <- TelegramTodoBot.make(client, store)
        _   <- bot.run.take(1).compile.drain
      } yield ()

      res.unsafeToFuture().map { r =>
        verify(client).pollUpdates
        verifyNoMoreInteractions(store)
        verify(client).send(ChatId(42), """Unable to add new todo item. Missing the actual item. Use "/help" for more information""")
        r mustBe ()
      }
    }

    "clear current todos" in {
      val (client, store) = mocks

      when(client.pollUpdates).thenReturn(Stream.emit(update(Some("/clear"))))
      when(store.clearAll(any[ChatId])).thenReturn(IO.unit)
      when(client.send(any[ChatId], any[String])).thenReturn(IO.unit)

      val res = for {
        bot <- TelegramTodoBot.make(client, store)
        _   <- bot.run.take(1).compile.drain
      } yield ()

      res.unsafeToFuture().map { r =>
        verify(client).pollUpdates
        verify(store).clearAll(ChatId(42L))
        verify(client).send(ChatId(42), "Your todo-list was cleared!")
        r mustBe ()
      }
    }

    "ignore unknown commands" in {
      val (client, store) = mocks

      when(client.pollUpdates).thenReturn(Stream.emit(update(Some("/foo"))))
      when(client.send(any[ChatId], any[String])).thenReturn(IO.unit)

      val res = for {
        bot <- TelegramTodoBot.make(client, store)
        _   <- bot.run.take(1).compile.drain
      } yield ()

      res.unsafeToFuture().map { r =>
        verify(client).pollUpdates
        verifyNoMoreInteractions(store)
        verify(client).send(ChatId(42), """Unrecognized command "/foo". type "/help" to see all available commands""")
        r mustBe ()
      }
    }

    "ignore messages from other bots" in {
      val (client, store) = mocks

      when(client.pollUpdates).thenReturn(Stream.emit(update(Some("/foo"), isBot = true)))

      val res = for {
        bot <- TelegramTodoBot.make(client, store)
        _   <- bot.run.take(1).compile.drain
      } yield ()

      res.unsafeToFuture().map { r =>
        verify(client).pollUpdates
        verifyNoMoreInteractions(store)
        verifyNoMoreInteractions(client)
        r mustBe ()
      }
    }

    "ignore messages with some words" in {
      val (client, store) = mocks

      when(client.pollUpdates).thenReturn(Stream.emit(update(Some("foo"))))

      val res = for {
        bot <- TelegramTodoBot.make(client, store)
        _   <- bot.run.take(1).compile.drain
      } yield ()

      res.unsafeToFuture().map { r =>
        verify(client).pollUpdates
        verifyNoMoreInteractions(store)
        verifyNoMoreInteractions(client)
        r mustBe ()
      }
    }

    "ignore messages with no text" in {
      val (client, store) = mocks

      when(client.pollUpdates).thenReturn(Stream.emit(update(None)))

      val res = for {
        bot <- TelegramTodoBot.make(client, store)
        _   <- bot.run.take(1).compile.drain
      } yield ()

      res.unsafeToFuture().map { r =>
        verify(client).pollUpdates
        verifyNoMoreInteractions(store)
        verifyNoMoreInteractions(client)
        r mustBe ()
      }
    }

    def mocks: (TelegramBotClient[IO], TodoStore[IO]) = {
      val client = mock[TelegramBotClient[IO]]
      val store  = mock[TodoStore[IO]]
      (client, store)
    }
  }

  def update(text: Option[String], isBot: Boolean = false): Update =
    Update(
      1L,
      Some(Message(
        1L,
        Chat(ChatId(42)),
        text,
        Some(MessageOrigin(Some(Username("Boris")), Some("Boris"), isBot, 1L))
      )),
      None
    )
}
