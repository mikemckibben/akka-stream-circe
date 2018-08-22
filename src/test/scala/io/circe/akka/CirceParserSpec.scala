package io.circe
package akka

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import _root_.akka.actor.ActorSystem
import _root_.akka.stream.ActorMaterializer
import _root_.akka.stream.scaladsl._
import _root_.akka.testkit.TestKit
import _root_.akka.util.ByteString
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{AsyncFunSpecLike, Matchers}

/** Test suite for `circe.akka` */
class CirceParserSpec extends TestKit(ActorSystem("CirceParserSpec")) with AsyncFunSpecLike with PropertyChecks with Matchers {
  private[this] implicit val materializer = ActorMaterializer()

  case class MyJson(value: Int)
  object MyJson {
    implicit val encoder = deriveEncoder[MyJson]
    implicit val decoder = deriveDecoder[MyJson]
  }

  val MyJsonGen = for {
    i <- Gen.choose(0, 100)
  } yield MyJson(i)

  val MyJsonVectorGen = Gen.listOf(MyJsonGen).map(_.toVector)

  describe("jsonValueStream") {
    describe("given source input type `ByteBuffer`") {
      it("should parse json values delimited by whitespace") {
        forAll (MyJsonVectorGen) { (values: Vector[MyJson]) =>
          val encoded = values.map(MyJson.encoder.apply)
          val result =
            Source(encoded)
              .via(binaryJsonValueStream)
              .map(_.toByteBuffer)
              .via(jsonValueStream)
              .runFold(Vector.empty[Json])((v, o) => v :+ o)

          Await.result(result, Duration.Inf) should equal (encoded)
        }
      }
    }

    describe("given source input type `ByteString`") {
      it("should parse json values delimited by whitespace") {
        forAll (MyJsonVectorGen) { (values: Vector[MyJson]) =>
          val encoded = values.map(MyJson.encoder.apply)
          val result =
            Source(encoded)
              .via(binaryJsonValueStream)
              .via(jsonValueStream)
              .runFold(Vector.empty[Json])((v, o) => v :+ o)

          Await.result(result, Duration.Inf) should equal (encoded)
        }
      }
    }

    describe("given source input type `String`") {
      it("should parse json values delimited by whitespace") {
        forAll (MyJsonVectorGen) { (values: Vector[MyJson]) =>
          val encoded = values.map(MyJson.encoder.apply)
          val result =
            Source(encoded)
              .via(stringJsonValueStream)
              .via(jsonValueStream)
              .runFold(Vector.empty[Json])((v, o) => v :+ o)

          Await.result(result, Duration.Inf) should equal (encoded)
        }
      }
    }
  }

  describe("jsonArrayStream") {
    describe("given source input type `ByteBuffer`") {
      it("should parse json values wrapped in an array") {
        forAll (MyJsonVectorGen) { (values: Vector[MyJson]) =>
          val encoded = values.map(MyJson.encoder.apply)
          val result =
            Source(encoded)
              .via(binaryJsonArrayStream)
              .map(_.toByteBuffer)
              .via(jsonArrayStream)
              .runFold(Vector.empty[Json])((v, o) => v :+ o)

          Await.result(result, Duration.Inf) should equal (encoded)
        }
      }
    }

    describe("given source input type `ByteString`") {
      it("should parse json values wrapped in an array") {
        forAll (MyJsonVectorGen) { (values: Vector[MyJson]) =>
          val encoded = values.map(MyJson.encoder.apply)
          val result =
            Source(encoded)
              .via(binaryJsonArrayStream)
              .via(jsonArrayStream)
              .runFold(Vector.empty[Json])((v, o) => v :+ o)

          Await.result(result, Duration.Inf) should equal (encoded)
        }
      }
    }

    describe("given source input type `String`") {
      it("should parse json values wrapped in an array") {
        forAll (MyJsonVectorGen) { (values: Vector[MyJson]) =>
          val encoded = values.map(MyJson.encoder.apply)
          val result =
            Source(encoded)
              .via(stringJsonArrayStream)
              .via(jsonArrayStream)
              .runFold(Vector.empty[Json])((v, o) => v :+ o)

          Await.result(result, Duration.Inf) should equal (encoded)
        }
      }
    }
  }

  describe("jsonEncode") {
    it("Should encode values as json") {
      forAll (MyJsonVectorGen) { (values: Vector[MyJson]) =>
        val result =
          Source(values)
            .via(jsonEncode)
            .runFold(Vector.empty[Json])((v, j) => v :+ j)

        Await.result(result, Duration.Inf) should equal (values.map(MyJson.encoder.apply))
      }
    }
  }

  describe("jsonDecode") {
    it("Should decode json values") {
      forAll (MyJsonVectorGen) { (values: Vector[MyJson]) =>
        val encoded = values.map(MyJson.encoder.apply)
        val result =
          Source(encoded)
            .via(jsonDecode[MyJson])
            .runFold(Vector.empty[MyJson])((v, o) => v :+ o)

        Await.result(result, Duration.Inf) should equal (values)
      }
    }
  }

  describe("binaryJsonValueStream") {
    it("should serialize json values delimited by newlines") {
      forAll { (values: Vector[Int]) =>
        val result =
          Source(values)
            .map(Json.fromInt)
            .via(binaryJsonValueStream)
            .runFold(ByteString.empty)(_ ++ _)

        Await.result(result, Duration.Inf) should equal(ByteString(values.mkString("\n")))
      }
    }
  }

  describe("binaryJsonArrayStream") {
    it("should serialize json values as an array stream") {
      forAll { (values: Vector[Int]) =>
        val result =
          Source(values)
            .map(Json.fromInt)
            .via(binaryJsonArrayStream)
            .runFold(ByteString.empty)(_ ++ _)

        Await.result(result, Duration.Inf) should equal(ByteString(values.mkString("[\n", ",\n", "\n]\n")))
      }
    }
  }

  describe("stringJsonValueStream") {
    it("should serialize json values delimited by newlines") {
      forAll { (values: Vector[Int]) =>
        val result =
          Source(values)
            .map(Json.fromInt)
            .via(stringJsonValueStream)
            .runFold("")(_ ++ _)

        Await.result(result, Duration.Inf) should equal (values.mkString("\n"))
      }
    }
  }

  describe("stringJsonArrayStream") {
    it("should serialize json values as an array stream") {
      forAll { (values: Vector[Int]) =>
        val result =
          Source(values)
            .map(Json.fromInt)
            .via(stringJsonArrayStream)
            .runFold("")(_ ++ _)

        Await.result(result, Duration.Inf) should equal (values.mkString("[\n", ",\n", "\n]\n"))
      }
    }
  }
}
