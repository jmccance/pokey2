package pokey.connection.actor

import akka.actor.ActorLogging
import pokey.connection.model.Command
import pokey.connection.model.Commands._
import pokey.user.model.User

trait CommandLoggers {
  this: ActorLogging =>

  private[this] type CommandLogger = Command => Command

  def logCommands(connUserId: User.Id): CommandLogger = logger {
    case ClearRoomCommand(roomId) => logMsg(connUserId, "clear", "roomId" -> roomId)

    case CreateRoomCommand => logMsg(connUserId, "createRoom")

    case JoinRoomCommand(roomId) => logMsg(connUserId, "joinRoom", "roomId" -> roomId)

    case KillConnectionCommand => logMsg(connUserId, "killConnection")

    case RevealRoomCommand(roomId) => logMsg(connUserId, "revealRoom", "roomId" -> roomId)

    case SetNameCommand(name) => logMsg(connUserId, "setName", "name" -> name)

    case SetTopicCommand(roomId, topic) =>
      logMsg(connUserId, "setTopic", "roomId" -> roomId, "topic" -> topic)

    case SubmitEstimateCommand(roomId, estimate) =>
      logMsg(connUserId, "submitEstimate", "roomId" -> roomId, "estimate" -> estimate)
  }

  private[this] def logMsg(userId: User.Id, command: String, fields: (String, Any)*): String = {
    (fields ++ Seq("userId" -> userId.toString, "command" -> command))
      .map { case (label, value) => s"$label: $value" }
      .mkString(", ")
  }

  private[this] def logger(pf: PartialFunction[Command, String]): CommandLogger = {
    PartialFunction { c =>
      pf.lift(c).foreach(log.info)
      c
    }
  }
}
