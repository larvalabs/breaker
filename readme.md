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

So when woken up, the websocket is receiving a message from either it's local connection (awaitResult instanceof WebSocketFrame) or from someone else connected to the room (awaitResult instanceof ChatRoomStream.Event). If the message is from the local websocket connection (and so a new message), it is queued in a SaveNewMessageJob to be saved to the database. It's also posted to a redis queue in case there are other servers in the cluster with clients connected.

Note: Avoiding contacting the database in order to process and distribute a message seems to be the key to a responsive server. When everything is happening locally in memory it's very fast, even on a fairly low powered heroku instance.

#### ChatRoomStream

ChatRoomStream is basically a list of recent ChatRoomStream.Event instances (messages, joins, leaves, etc) for a room that you can wait to listen to new messages from. Upon initial connection it will send out the last STREAM_SIZE of messages to a new client. This is how the websocket receives new messages across rooms.

#### RedisQueueJob

This is the long-running job that's each server's connection to the redis queue.

### Frontend

The frontend is a super simple jquery single page type thing. There's an ultra-simple javascript templating system that generates pieces of the page (messages, user status entries, etc). It's slightly budget but it works ok. It's all in room3.html. I used a template for the design which is probably not coded that well, is fairly complicated to work with, and has some bugs. A whole range of front end changes are up for discussion.

### Misc

#### RedditLinkBotJob

There's a bot job that goes out and look up new top links for the room and posts them. This is slightly distracting, and a bit of a downside in inactive rooms. Up for discussion if we should even be running it anymore.

#### NotifyMentionedUsersJob

If the SaveNewMessageJob notices that there might a user mention in the message it triggers NotifyMentionedUsersJob to send PMs to all mentioned users on reddit. The account that sends these mentions out might have gotten banned because people were spamming out mentions, which is another thing to figure out in general.

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
