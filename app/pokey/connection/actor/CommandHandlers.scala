package pokey.connection.actor

import akka.actor.ActorRef
import pokey.connection.model.Command
import pokey.connection.model.Commands._
import pokey.connection.model.Events.ErrorEvent
import pokey.room.actor.RoomProxyActor

trait CommandHandlers {
  def setTopicCommandHandler(
    client: ActorRef,
    connUserId: String,
    rooms: Map[String, ActorRef]
  )(implicit sender: ActorRef): PartialFunction[Command, Unit] = {
    case SetTopicCommand(roomId, topic) =>
      //      log.info(
      //        "userId: {}, command: setTopic, roomId: {}, topic: {}",
      //        connUserId, roomId, topic
      //      )

      rooms.get(roomId) match {
        case Some(roomProxy) =>
          roomProxy ! RoomProxyActor.SetTopic(connUserId, topic)

        case None =>
          client ! ErrorEvent(s"Room $roomId is not associated with this connection")
      }
  }
}

object CommandHandlers extends CommandHandlers
