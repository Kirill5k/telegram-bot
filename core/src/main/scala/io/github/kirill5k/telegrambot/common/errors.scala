package io.github.kirill5k.telegrambot.common

object errors {

  sealed trait AppError extends Throwable {
    def message: String
    override def getMessage: String = message
  }
}
