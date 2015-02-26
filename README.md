Pokey v2
========

## Protocol

The client initially acquires a *session id* by accessing any asset. The session id is stored in the
Play session cookie, which is signed by the application to prevent tampering.

The session id provides a unique identifier for the user. On connecting to the websocket endpoint at
`/socket`, the session id will be validated or else the connection will be rejected.

Once the connection is granted, the client can send one of a number of requests. Each request type
is distinguished by the "request" field. All requests are defined in pokey.websocket.Requests.

* SetName
* CreateRoom
* JoinRoom
* Estimate
* Reveal
* Hide
* Clear

