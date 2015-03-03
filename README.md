Pokey v2
========

**NOTE: Pokey v2 is still in development and is not functional in any way, shape, or form.**

## Roadmap

* 2.0 - Feature parity with Pokey 1.0 with rewritten back-end (and necessary changes to front-end).
* 2.1 - Support for alternative estimating schemes, including integer ranges, Fibonacci numbers,
        and t-shirt sizing.

## Implementation

### Terminology

A *user* is identified by their *user id*.

A user may have one or more WebSocket *connections* to the application.

A *room* is an aggregation of connections, owned by a particular user. In a room, each connection
will be associated with an optional *estimate*. An estimate consists of a value and an optional
comment. A room may be *revealed*, meaning that members can see each other's estimates, or *hidden*,
meaning that they can only see whether or not a user has estimated. Revealing and hiding can only
be done by the room's owner.

A user will only persist for a finite interval after their last connection is closed. If a user
*expires*, their rooms will as well, meaning that room membership and estimates will not persist.

### Client/Server View

The client initially acquires a user id by accessing any asset (e.g., `GET /`). The user id is
stored in the Play session cookie, which is signed by the application to prevent tampering. Once the
 client has a user id, it can establish a WebSocket connection with the `/connect` endpoint.

Once the connection is granted, the client interacts with the application by sending and receiving
JSON objects over the WebSocket connection. Requests and server events are defined in the Requests
and Events objects in the pokey.connection package.

#### Requests

* SetName - Update the name that will be displayed to other users.
* CreateRoom - Creates a new room, owned by the current user, that other users can join and submit
               estimates. Note that creating a room and joining a room are separate requests.
* JoinRoom - Joins the specified estimation room. Required in order to send estimates to that room.
* Estimate - Submit an estimate to the specified room. Client must have joined the room first.
* Reveal - Reveal all members' estimates in the specified room to all members of the room. User must
           be the room owner in order to reveal.
* Clear - Reset all members' estimates in the specified room and hide the estimates. User must be
          the owner in order to clear.

#### Events

* UserUpdated - Emitted when a user has changed. Clients will receive UserUpdated messages for their
                own user, as well as any users in any rooms the connection has joined.
* RoomCreated - Emitted when the connection successfully creates a room via a CreateRoom request.
* RoomInfo - Emitted both when the connection first joins the room and whenever the room info
             changes. Room info includes the room id, the room owner's user id, and the roster of
             connected users (names and ids).
* RoomState - Emitted both when the connection first joins (after the RoomInfo message is emitted)
              and whenever the room state changes. Contains the room id, whether the room is
              revealed, and a map from user ids to their (optional) estimates.
* ErrorEvent - Sent whenever an error needs to be communicated to the client. Generally when a
               request is sent that is either invalid or not completable.

### Backend Architecture

> Topics to cover:
> * Proxy/Registry/Service pattern
> * Subscribable trait
> * The event-sourcing-ish approach we're using.
> * Rich models that fully encapsulate the functional requirements. (E.g., only owners can reveal a
    room, you must be in a room to estimate, etc.)
> * Handling ask pattern usage. Wrap in service components in order to limit amount of boilerplate
    mapTo and timeout logic. Clarifies the expected "return type" of an ask.
