package pokey.connection.actor

import akka.actor.{ActorRef, PoisonPill}
import akka.pattern.pipe
import pokey.connection.actor.ConnectionHandler.RoomJoined
import pokey.connection.model.Commands._
import pokey.connection.model.Events.ErrorEvent
import pokey.connection.model.{Command, Events}
import pokey.room.actor.RoomProxyActor
import pokey.room.service.RoomService
import pokey.user.actor.{UserProxy, UserProxyActor}

import scala.concurrent.ExecutionContext

object CommandHandlers {
  private[this]type Handler[A <: Command] = A => Unit

  def handleCommandWith(
    client: ActorRef,
    connUserId: String,
    rooms: Map[String, ActorRef],
    roomService: RoomService,
    userProxy: UserProxy
  )(implicit
    self: ActorRef,
    ec: ExecutionContext): Command => Unit = {
    // The pattern of assigning the handler to a value before invoking it seems to be necessary
    // because of the way we're using implicits here. There is no good way that I can find to get
    // the value of a function that takes more than one implicit parameter (and it seems to only
    // apply to functions with more than one implicit) and then immediately apply it to a value.
    //
    // So while the below is definitely ugly and noisy, it at least avoids lots of painful, manual
    // implicit passing while still preserving type safety.

    case c: ClearRoomCommand =>
      val h = handleClearRoomCommand(client, connUserId, rooms)
      h(c)

    case CreateRoomCommand =>
      val h = handleCreateRoomCommand(client, connUserId, roomService)
      h(CreateRoomCommand)

    case c: JoinRoomCommand =>
      val h = handleJoinRoomCommand(client, roomService, userProxy)
      h(c)

    case KillConnectionCommand =>
      val h = handleKillConnectionCommand
      h(KillConnectionCommand)

    case c: RevealRoomCommand =>
      val h = handleRevealRoomCommand(client, connUserId, rooms)
      h(c)

    case c: SetNameCommand =>
      val h = handleSetNameCommand(userProxy)
      h(c)

    case c: SetTopicCommand =>
      val h = handleSetTopicCommand(client, connUserId, rooms)
      h(c)

    case c: SubmitEstimateCommand =>
      val h = handleSubmitEstimateCommand(client, connUserId, rooms)
      h(c)

    case c: InvalidCommand =>
      val h = handleInvalidCommand(client, connUserId)
      h(c)
  }

  def handleClearRoomCommand(
    client: ActorRef,
    connUserId: String,
    rooms: Map[String, ActorRef]
  )(implicit self: ActorRef): Handler[ClearRoomCommand] = {
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
  )(implicit self: ActorRef, ec: ExecutionContext): Handler[CreateRoomCommand.type] = { _ =>
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
    ec: ExecutionContext): Handler[JoinRoomCommand] = {
    case JoinRoomCommand(roomId) =>
      roomService.getRoom(roomId).foreach {
        case Some(roomProxy) =>
          roomProxy.ref ! RoomProxyActor.JoinRoom(userProxy)
          self ! RoomJoined(roomProxy)

        case None => client ! Events.ErrorEvent(s"No room found with id '$roomId'")
      }
  }

  def handleKillConnectionCommand(implicit self: ActorRef): Handler[KillConnectionCommand.type] =
    { _ => self ! PoisonPill }

  def handleRevealRoomCommand(
    client: ActorRef,
    connUserId: String,
    rooms: Map[String, ActorRef]
  )(implicit self: ActorRef): Handler[RevealRoomCommand] = {
    case RevealRoomCommand(roomId) =>
      rooms.get(roomId) match {
        case Some(roomProxy) => roomProxy ! RoomProxyActor.RevealFor(connUserId)

        case None => client ! ErrorEvent(s"Room $roomId is not associated with this connection")
      }
  }

  def handleSetNameCommand(
    userProxy: UserProxy
  )(implicit self: ActorRef): Handler[SetNameCommand] = {
    case SetNameCommand(name) => userProxy.ref ! UserProxyActor.SetName(name)
  }

  def handleSetTopicCommand(
    client: ActorRef,
    connUserId: String,
    rooms: Map[String, ActorRef]
  )(implicit self: ActorRef): Handler[SetTopicCommand] = {
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
  )(implicit self: ActorRef): Handler[SubmitEstimateCommand] = {
    case SubmitEstimateCommand(roomId, estimate) =>
      rooms.get(roomId) match {
        case Some(roomProxy) =>
          roomProxy ! RoomProxyActor.SubmitEstimate(connUserId, estimate)

        case None =>
          client ! ErrorEvent(s"Room $roomId is not associated with this connection")
      }
  }

  def handleInvalidCommand(
    client: ActorRef, connUserId: String
  )(implicit self: ActorRef): Handler[InvalidCommand] = {
    case InvalidCommand(json) => client ! Events.ErrorEvent("Invalid command")
  }
}
