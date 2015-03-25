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

### Web API

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

### Server View

#### Code Organization

The server-side code is organized by feature as much as possible. The top-level packages under
"pokey" are:

* application
    * Initialization and configuration of the application, including the Scaldi DI modules and the
    Play Global object.
* assets
    * The serving of static assets. This includes adding the user id to each visitor's Play
    session.
* common
    * Cross-cutting concerns. Mainly errors at the moment.
* connection
    * Client WebSocket connections.
* room
    * Estimation rooms management.
* user
    * User management.
* util
    * Things that don't quite fit elsewhere.

Each package is divided into some consistent, self-explanatory subpackages based on functionality:

* actor
* controller
* model
* service

#### Patterns

The "room" and "user" features are organized into an important pattern of *model*, *proxy*,
*registry*, and *service*.

The *models* consist of conventional Scala objects. As much as possible, these encapsulate the
entirety of the business logic. For example a Room object encapsulates all the ideas of room
membership, who's allowed to reveal or clear a room, who can submit an estimate, and so forth. This
is intended to simplify the other parts of the application by keeping important functional logic in
obvious, consolidated places.

Since models are immutable but the state of the application changes over time, the canonical
instance of each model is wrapped in a *proxy* actor. These actors are responsible for coordinating
changes to the models through message passing. Additionally, both the RoomProxyActor and the
UserProxyActor make use of the util.Subscribable trait. This allows, for example, the RoomProxyActor
to subscribe to changes to a User in order to update its internal Room model.

Proxy objects like RoomProxy and UserProxy allow for easily passing around an object that contains
both the id of an entity and the ActorRef for its proxy actor.

To manage the creation and access of proxies we use another actor, the *registry*.

Finally, in order to simplify testing and avoid duplicated logic around the ask pattern, common
asks for a given registry are implemented in a *service*. This exposes conventional, Future\[T\]
-returning service methods that both avoid duplicated code and keep actor code out of the
controllers.

### Client View

#### TODOC

* Flux/React implementation.
* Naming conventions. React components are CamelCased, other files are lowerCamelCased.
* The primary stores and their roles.
    * ContextStore
    * RoomStore
    * EstimateStore
* Action integrations
    * Connection - Interacts with the WebSocket API, generating ConnectionEvents.
    * Router - Manages hash-routing, triggering events to the Dispatcher when the route changes.
