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
*expires*, their rooms will as well. When this happens, the room *closes* and all associated data
is released.

### Client/Server View

The client initially acquires a user id by accessing any asset (e.g., `GET /`). The user id is
stored in the Play session cookie, which is signed by the application to prevent tampering. Once the
 client has a user id, it can establish a WebSocket connection with the `/connect` endpoint.

Once the connection is granted, the client interacts with the application by sending and receiving
JSON objects over the WebSocket connection. In the long run the intention is to follow a 
[CQRS](http://martinfowler.com/bliki/CQRS.html) pattern, though at the moment there are only
"commands" and no "queries".

Generally speaking, the server does not send a response specific to a command. Instead any
appropriate feedback will be sent in the form of event messages. For example a "SetName" command
will trigger a "UserUpdated" event in response, since all users are subscribed to changes to
themselves.

For the details of what each message includes, see pokey.connection.model.Commands and
pokey.connection.model.Events.

### Backend Architecture



> Topics to cover:
> * Proxy/Registry/Service pattern
> * Subscribable trait
> * The event-sourcing-ish approach we're using.
> * Rich models that fully encapsulate the functional requirements. (E.g., only owners can reveal a
    room, you must be in a room to estimate, etc.)
> * Handling ask pattern usage. Wrap in service components in order to limit amount of boilerplate
    mapTo and timeout logic. Clarifies the expected "return type" of an ask.
