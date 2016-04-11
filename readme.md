## Overview

### Backend

The server runs on Play 1.x: https://playframework.com/documentation/1.4.x/home which is one of our favorite web frameworks, but isn't as popular as it once was. The nice thing is it has plenty of conveniences, but is still Java and has proper websocket and client suspension, so is a pretty good choice for a server supporting lots of persistent connections. We use Postgres and Redis for data storage. The server is hosted on Heroku. We currently are running a single server, but cluster support exists and *probably* works great!

In terms of main classes, on the server side most of the work is done by a few classes.

#### Application

This is the main page serving class. Most of the public static methods are routes, as per Play convention.

#### WebSocket

This is the class that sets up and maintains the persistent websocket connection to clients directly connected to each server instance. The actual websocket class is the inner class WebSocket.ChatRoomSocket, it's mapped via the routes file to /websocket/room/socket, so the client connects directly to it once the page is served and started.

The WebSocket.ChatRoomSocket works by first loading a bunch of information about the user and all the rooms the user is a part of. This is stored in a big map to avoid database lookups later when processing messages. After that, the websocket *awaits* an event on any of the room's streams, as well as the locally connected websocket (see next section for stream description). Look for this line:

```
Object awaitResult = await(Promise.waitAny(roomEventPromises));
```

So when woken up, the websocket is receiving a message from either it's local connection (awaitResult instanceof WebSocketFrame) or from someone else connected to the room (awaitResult instanceof ChatRoomStream.Event). It also is queued in a job to be saved to the database if necessary. It's also posted to a redis queue in case there are other servers in the cluster with clients connected.

#### ChatRoomStream

ChatRoomStream is basically a list of recent ChatRoomStream.Event instances (messages, joins, leaves, etc) for a room that you can wait to listen to new messages from. Upon initial connection it will send out the last STREAM_SIZE of messages to a new client. This is how the websocket receives new messages across rooms.

#### RedisQueueJob

This is the long-running job that's each server's connection to the redis queue.

Postgres via Brew instructions
===

If you've installed postgres via brew then you can probably control it via these commands:
brew services start postgres
brew services restart postgres

And if your machine has crashed it might have left a pid file behind and won't let you start the server, do this:
rm /usr/local/var/postgres/postmaster.pid
brew services start postgres

Redis instructions
===

Install the play module:

play install redis
play deps

Hopefully the library dependency in the idea project points to the right place in your project so you can compile.

Now, install redis:

brew install redis
(this will show some info, or later you can run 'brew info redis')
launchctl load ~/Library/LaunchAgents/homebrew.mxcl.redis.plist
