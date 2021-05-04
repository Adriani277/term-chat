package termchat

import zio.test._
import zio.test.Assertion._
import zio._
import zhttp.socket.WebSocketFrame
import zio.stream.ZSink
import zio.stream.ZStream

object SubscriberSpec extends DefaultRunnableSpec {
  def spec: ZSpec[Environment, Failure] = suite("subscribe")(
    testM("sample") {
      assertM(ZStream.succeed(1).runHead)(equalTo(Some(1)))
    },
    testM("returns a Ping when a subscriber pulls its own message") {

      val result =
        Hub
          .bounded[(String, WebSocketFrame.Text)](10)
          .flatMap { hub =>
            //     hub.subscribe.use { s =>
            //       hub
            //         .publish(("user", WebSocketFrame.Text("test"))) *>
            //         s.poll.flatMap(ZIO.fromOption(_)).map(_._2)
            //     }
            (ZStream
              .fromHub(hub) <& ZStream
              .fromEffect(hub.publish(("user", WebSocketFrame.Text("test"))))
              .forever)
              .take(1)
              .map(_._2)
              .tap {
                case WebSocketFrame.Text(s) => console.putStrLn(s)
                case _                      => UIO.unit
              }
              .runHead

            // Subscriber
            //   .subscribe("user", hub)
            //   .tap {
            //     case WebSocketFrame.Text(s) => console.putStrLn(s)
            //     case _                      => UIO.unit
            //   }
            //   .take(1)
            //   .runHead
          }

      assertM(result)(equalTo(Some(WebSocketFrame.Text("test"))))
    }
  )
}
