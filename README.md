# akka-stream-circe

Akka stream support for streaming JSON parsing and decoding with [io.circe](https://io.circe.github.io/io.circe/).

## JSON Streaming Parser

This library provides two different stype of JSON stream parser `Flows`:

- newline separated stream of JSON values
```json
{ "timestamp": 1529247321000, "value": "value 1" }
{ "timestamp": 1529333721000, "value": "value 2" }
```
- unwrapped array stream of JSON value
```json
[
  { "timestamp": 1529247321000, "value": "value 1" },
  { "timestamp": 1529333721000, "value": "value 2" }
]
```

Supported flow input value types:
- akka.util.ByteString
- java.nio.ByteBuffer
- String


## JSON Encoding

Encoding values with a defined `Encoder` in scope as JSON AST is supported via the  `io.circe.akka.jsonEncode` method.
```scala
def jsonEncode[A : Encoder]: Flow[A, Json, NotUsed] = ???
```

## JSON Decoding

Decoding io.circe JSON AST values to values with a defined `Decoder` in scope is supported via the `io.circe.akka.jsonDecode`
method.
```scala
def jsonDecode[A : Decoder]: Flow[Json, A, NotUsed] = ???
```

### Example

```scala
import akka.NotUsed
import akka.stream._
import akka.stream.scaladsl._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.akka.{jsonValueStream, jsonDecode}

case class MyModel(timestamp: Long, value: String)

val rawStringStreamSource: Source[String, NotUsed] = Source(List("""{ "timestamp": 1529247321000, "value": "value 1" }\n""", """{ "timestamp": 1529333721000, "value": "value 2" }\n"""))
val jsonValues = rawStringStreamSource.via(jsonValueStream)
val myModelValues = jsonValues.via(jsonDecode[MyModel])
```

# License

io.circe-akka is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
