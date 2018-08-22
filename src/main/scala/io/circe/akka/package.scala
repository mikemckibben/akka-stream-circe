package io.circe

import _root_.akka.NotUsed
import _root_.akka.stream.scaladsl.Flow
import _root_.akka.stream.{FlowShape, Graph}
import _root_.akka.util.ByteString
import _root_.jawn.AsyncParser
import io.circe.jawn.CirceSupportParser

package object akka {

  def jsonArrayStream[I](implicit parser: ParserInput[I]): Graph[FlowShape[I, Json], NotUsed] =
    new CirceParserFlow[I](CirceSupportParser.async(AsyncParser.UnwrapArray))

  def jsonValueStream[I](implicit parser: ParserInput[I]): Graph[FlowShape[I, Json], NotUsed] =
    new CirceParserFlow[I](CirceSupportParser.async(AsyncParser.ValueStream))

  def jsonDecode[A : Decoder]: Flow[Json, A, NotUsed] = {
    Flow[Json].map(
      _.as[A].fold(
        e => throw e,
        identity
      )
    )
  }

  def jsonEncode[A : Encoder]: Flow[A, Json, NotUsed] = Flow[A].map(Encoder[A].apply)

  def binaryJsonArrayStream: Flow[Json, ByteString, NotUsed] = {
    Flow[Json]
      .map(js => ByteString(Printer.noSpaces.prettyByteBuffer(js)))
      .intersperse(ByteString("[\n"), ByteString(",\n"), ByteString("\n]\n"))
  }

  def binaryJsonValueStream: Graph[FlowShape[Json, ByteString], NotUsed] = {
    Flow[Json]
      .map(js => ByteString(Printer.noSpaces.prettyByteBuffer(js)))
      .intersperse(ByteString("\n"))
  }

  def stringJsonArrayStream: Flow[Json, String, NotUsed] = {
    Flow[Json]
      .map(Printer.noSpaces.pretty)
      .intersperse("[\n", ",\n", "\n]\n")
  }

  def stringJsonValueStream: Graph[FlowShape[Json, String], NotUsed] = {
    Flow[Json]
      .map(Printer.noSpaces.pretty)
      .intersperse("\n")
  }

}
