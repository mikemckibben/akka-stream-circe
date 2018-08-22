package io.circe
package akka

import _root_.akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import _root_.akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import _root_.jawn.AsyncParser

/**
  * Akka [[_root_.akka.stream.javadsl.Flow]] for streaming json parsing using circe with jawn [[AsyncParser]].
  *
  * Default implicit [[AsyncParser]] instances are defined for
  * `akka.util.ByteString`, `java.nio.ByteBuffer`, `Array[Byte]`, and `String`.
  *
  * @param parserFactory jawn async parser for circe
  * @param parserInput source parser input typeclass
  * @tparam I
  */
class CirceParserFlow[I](parserFactory: => AsyncParser[Json])(implicit parserInput: ParserInput[I]) extends GraphStage[FlowShape[I, Json]] {
  private[this] val in = Inlet[I]("CirceParserFlow.in")
  private[this] val out = Outlet[Json]("CirceParserFlow.out")

  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      private[this] val parser = parserFactory

      private[this] def emitChunk(chunk: Either[Throwable, Seq[Json]], last: Boolean = false): Unit = {
        chunk.fold(
          e => failStage(e),
          values => if (values.nonEmpty) emitMultiple(out, values.toVector) else if (!last) pull(in)
        )
      }

      setHandler(
        in,
        new InHandler {
          override def onPush(): Unit = {
            emitChunk(parserInput.acceptInput(parser, grab(in)))
          }
          override def onUpstreamFinish(): Unit = {
            // complete any leftovers in the buffer before completing the stage
            emitChunk(parserInput.complete(parser), true)
            completeStage()
          }
        }
      )

      setHandler(
        out,
        new OutHandler {
          override def onPull(): Unit = pull(in)
        }
      )
    }
  }
}
