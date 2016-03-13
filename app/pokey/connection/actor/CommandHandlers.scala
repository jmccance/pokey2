package pokey.connection.actor

import akka.actor.{ ActorRef, PoisonPill }
import akka.pattern.pipe
import pokey.connection.actor.ConnectionHandler.RoomJoined
import pokey.connection.model.Commands._
import pokey.connection.model.Events.ErrorEvent
import pokey.connection.model.{ Command, Events }
import pokey.room.actor.RoomProxyActor
import pokey.room.service.RoomService
import pokey.user.actor.{ UserProxy, UserProxyActor }

import scala.concurrent.ExecutionContext

object CommandHandlers {
  type CommandHandler = PartialFunction[Command, Unit]

  def handleCommandWith(
    client: ActorRef,
    connUserId: String,
    rooms: Map[String, ActorRef],
    roomService: RoomService,
    userProxy: UserProxy
  )(implicit
    self: ActorRef,
    ec: ExecutionContext): CommandHandler =
    (handleClearRoomCommand(client, connUserId, rooms)
      orElse handleCreateRoomCommand(client, connUserId, roomService)
      orElse handleJoinRoomCommand(client, roomService, userProxy)
      orElse handleKillConnectionCommand
      orElse handleRevealRoomCommand(client, connUserId, rooms)
      orElse handleSetNameCommand(userProxy)
      orElse handleSetTopicCommand(client, connUserId, rooms)
      orElse handleSubmitEstimateCommand(client, connUserId, rooms)
      orElse handleInvalidCommand(client, connUserId))

  def handleClearRoomCommand(
    client: ActorRef,
    connUserId: String,
    rooms: Map[String, ActorRef]
  ): CommandHandler = {
    case ClearRoomCommand(roomId) =>
      rooms.get(roomId) match {
        case Some(roomProxy) => roomProxy ! RoomProxyActor.ClearFor(connUserId)

        case None => client ! ErrorEvent(s"Room $roomId is not associated with this connection")
      }
  }

  def handleCreateRoomCommand(
    client: ActorRef,
    connUserId: String,
    roomService: RoomService
  )(implicit ec: ExecutionContext): CommandHandler = {
    case CreateRoomCommand =>
      roomService
        .createRoom(connUserId)
        .map(proxy => Events.RoomCreatedEvent(proxy.id))
        .recover(ErrorEvent.mapThrowable)
        .pipeTo(client)
      ()
  }

  def handleJoinRoomCommand(
    client: ActorRef,
    roomService: RoomService,
    userProxy: UserProxy
  )(implicit
    self: ActorRef,
    ec: ExecutionContext): CommandHandler = {
    case JoinRoomCommand(roomId) =>
      roomService.getRoom(roomId).foreach {
        case Some(roomProxy) =>
          roomProxy.ref ! RoomProxyActor.JoinRoom(userProxy)
          self ! RoomJoined(roomProxy)

        case None => client ! Events.ErrorEvent(s"No room found with id '$roomId'")
      }
  }

  def handleKillConnectionCommand(implicit self: ActorRef): CommandHandler = {
    case KillConnectionCommand => self ! PoisonPill
  }

  def handleRevealRoomCommand(
    client: ActorRef,
    connUserId: String,
    rooms: Map[String, ActorRef]
  ): CommandHandler = {
    case RevealRoomCommand(roomId) =>
      rooms.get(roomId) match {
        case Some(roomProxy) => roomProxy ! RoomProxyActor.RevealFor(connUserId)

        case None => client ! ErrorEvent(s"Room $roomId is not associated with this connection")
      }
  }

  def handleSetNameCommand(userProxy: UserProxy): CommandHandler = {
    case SetNameCommand(name) => userProxy.ref ! UserProxyActor.SetName(name)
  }

  def handleSetTopicCommand(
    client: ActorRef,
    connUserId: String,
    rooms: Map[String, ActorRef]
  )(implicit self: ActorRef): CommandHandler = {
    case SetTopicCommand(roomId, topic) =>
      rooms.get(roomId) match {
        case Some(roomProxy) =>
          roomProxy ! RoomProxyActor.SetTopic(connUserId, topic)

        case None =>
          client ! ErrorEvent(s"Room $roomId is not associated with this connection")
      }
  }

  def handleSubmitEstimateCommand(
    client: ActorRef,
    connUserId: String,
    rooms: Map[String, ActorRef]
  ): CommandHandler = {
    case SubmitEstimateCommand(roomId, estimate) =>
      rooms.get(roomId) match {
        case Some(roomProxy) =>
          roomProxy ! RoomProxyActor.SubmitEstimate(connUserId, estimate)

        case None =>
          client ! ErrorEvent(s"Room $roomId is not associated with this connection")
      }
  }

  def handleInvalidCommand(client: ActorRef, connUserId: String): CommandHandler = {
    case InvalidCommand(json) => client ! Events.ErrorEvent("Invalid command")
  }
}
