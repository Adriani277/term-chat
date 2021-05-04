package termchat

import zhttp.socket.WebSocketFrame
import zio._
import zio.stream._

object Subscriber {
  def subscribe(
      name: String,
      subscriptionQueue: ZDequeue[Any, Nothing, (String, WebSocketFrame.Text)]
  ): ZStream[Any, Nothing, WebSocketFrame] =
    ZStream.fromQueue(subscriptionQueue).map { case (user, msg) =>
      if (user == name)
        WebSocketFrame.Ping
      else
        WebSocketFrame.text(user + ":" + msg.text)
    }
}
