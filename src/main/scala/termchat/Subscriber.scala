package termchat

import zhttp.socket.WebSocketFrame
import zio.stream.UStream
import zio._

object Subscriber {
  def subscribe(
      name: String,
      hub: Hub[(String, WebSocketFrame.Text)]
  ): UStream[WebSocketFrame] = UStream.fromHub(hub).map { case (user, msg) =>
    if (user == name)
      WebSocketFrame.Ping
    else
      WebSocketFrame.text(user + ":" + msg.text)
  }
}
