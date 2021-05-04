package termchat

import zhttp.http._
import zhttp.service._
import zhttp.socket._
import zio._
import zio.stream.ZStream

// TODO: Type alias App
object Main extends App {
  private def socket(
      hub: Hub[(String, WebSocketFrame.Text)],
      name: String
  ): Socket[Any, SocketError, WebSocketFrame, WebSocketFrame] =
    Socket.collect[WebSocketFrame] {
      case WebSocketFrame.Text("subscribe\n") =>
        for {
          queue <- ZStream.managed(hub.subscribe)
          res   <- Subscriber.subscribe(name, queue)
        } yield res

      case fr @ WebSocketFrame.Text(_) =>
        ZStream
          .fromEffect(
            hub.publish((name, fr))
          )
          .as(WebSocketFrame.Ping)
    }

  private val app =
    Http.collectM {
      case Method.GET -> Root / "greet" / name           =>
        ZIO.succeed(Response.text(s"Greetings {$name}!"))
      case req @ Method.GET -> Root / "subscribe" / name =>
        for {
          queue <- ZIO.service[Hub[(String, WebSocketFrame.Text)]]
        } yield Response.socket(socket(queue, name))
    }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    Server
      .start(8090, app)
      .provideCustomLayer(
        ZLayer.fromEffect(Hub.sliding[(String, WebSocketFrame.Text)](100))
      )
      .exitCode
}
