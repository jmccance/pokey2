Pokey v2
========

**NOTE: Pokey v2 is still in development and is not functional in any way, shape, or form.**

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

> Could provide some fake durability here. Say that a room is calculated from the owner id and the
> timestamp of creation. If the user expires but someone joins the room, we could theoretically let
> them join it and have the user with that session id own it when they next connect.

### Protocol

The client initially acquires a *session id* by accessing any asset. The session id is stored in the
Play session cookie, which is signed by the application to prevent tampering.

The session id provides a unique identifier for the user. On connecting to the websocket endpoint at
`/connect`, the session id will be validated or else the connection will be rejected.

Once the connection is granted, the client can send one of a number of requests. Each request type
is distinguished by the "request" field. All requests are defined in pokey.websocket.Requests.

* SetName - Changes the user's displayed name
* CreateRoom - Creates a new room, owned by the current user, that other user's can join and submit
               estimates.
* JoinRoom - Joins the specified room. The user will show up in the room's status updates.
* Estimate - Submit an estimate to the specified room. If the room is revealed, the estimate is
             displayed. If the room is not revealed, the room's status update will simply indicate
             that the user has submitted an estimate.
* Reveal - Reveal all members' estimates in the specified room to all members of the room.
* Clear - Reset all members' estimates in the specified room and hide the estimates.
