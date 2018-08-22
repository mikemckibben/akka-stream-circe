package io.circe
package akka

import java.nio.ByteBuffer

import _root_.akka.util.ByteString
import _root_.jawn.AsyncParser
import jawn.CirceSupportParser.facade

/** Typeclass for streaming json parser for a given source input type `I` */
sealed trait ParserInput[I] {

  /** accept a chunk of input and output any completed json values */
  def acceptInput(parser: AsyncParser[Json], chunk: I): Either[Throwable, Seq[Json]]

  /** signal end of input and output any remaining leftover json values from the
    * buffered input */
  final def complete(parser: AsyncParser[Json]): Either[Throwable, Seq[Json]] =
    parser.finish()

}

object ParserInput {

  implicit def byteString: ParserInput[ByteString] =
    new ParserInput[ByteString] {
      def acceptInput(parser: AsyncParser[Json], chunk: ByteString): Either[Throwable, Seq[Json]] =
        parser.absorb(chunk.toByteBuffer)
    }

  implicit def byteBuffer: ParserInput[ByteBuffer] =
    new ParserInput[ByteBuffer] {
      def acceptInput(parser: AsyncParser[Json], chunk: ByteBuffer): Either[Throwable, Seq[Json]] =
        parser.absorb(chunk)
    }

  implicit def byteArray: ParserInput[Array[Byte]] =
    new ParserInput[Array[Byte]] {
      def acceptInput(parser: AsyncParser[Json], chunk: Array[Byte]): Either[Throwable, Seq[Json]] =
        parser.absorb(chunk)
    }

  implicit def string: ParserInput[String] =
    new ParserInput[String] {
      def acceptInput(parser: AsyncParser[Json], chunk: String): Either[Throwable, Seq[Json]] =
        parser.absorb(chunk)
    }
}
