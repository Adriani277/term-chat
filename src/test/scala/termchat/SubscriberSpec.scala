package termchat

import zio.test._
import zio.test.Assertion._
import zio._
import zhttp.socket.WebSocketFrame
import zio.stream.ZStream
import zio.duration.Duration
import java.util.concurrent.TimeUnit

object SubscriberSpec extends DefaultRunnableSpec {
  def spec: ZSpec[Environment, Failure] = suite("subscribe")(
    testM("sample") {
      assertM(ZStream.succeed(1).runHead)(equalTo(Some(1)))
    },
    testM("returns a Ping when a subscriber pulls its own message") {
      checkM(Gen.anyString, Gen.anyString.map(WebSocketFrame.Text(_))) { (user, msg) =>
        val result =
          for {
            hub   <- ZStream.fromEffect(
                       Hub.bounded[(String, WebSocketFrame.Text)](16)
                     )
            queue <- ZStream.managed(hub.subscribe)
            msg   <- Subscriber.subscribe(user, queue) <& ZStream
                       .fromEffect(
                         hub.publish((user, msg))
                       )
          } yield msg
        assertM(result.runHead)(equalTo(Some(WebSocketFrame.Ping)))
      }
    } @@ TestAspect.timeout(Duration(5, TimeUnit.SECONDS)),
    testM(
      "returns the message when a subscriber pulls a different user message"
    ) {
      val gen = for {
        user1 <- Gen.anyString
        user2 <- Gen.anyString.filterNot(_ == user1)
        msg   <- Gen.anyString.map(WebSocketFrame.Text(_))
      } yield (user1, user2, msg)
      checkM(gen) { case (user1, user2, msg) =>
        val result =
          for {
            hub   <- ZStream.fromEffect(
                       Hub.bounded[(String, WebSocketFrame.Text)](16)
                     )
            queue <- ZStream.managed(hub.subscribe)
            text  <- Subscriber.subscribe(user1, queue) <& ZStream
                       .fromEffect(
                         hub.publish((user2, msg))
                       )
          } yield text

        assertM(result.runHead)(
          isSome(equalTo(WebSocketFrame.Text(user2 + ":" + msg.text)))
        )
      }
    } @@ TestAspect.timeout(Duration(5, TimeUnit.SECONDS))
  )
}
