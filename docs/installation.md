# Installation instructions
To install Breaker for local development, clone your fork and follow the Backend, Frontend, and Configuration instructions below. 

After installation, run the app using `play run` in the project directory and open `localhost:9000`.

If you have any issues, feel free to ping us in [Breaker](http://breakerapp.com/r/breakerapp).

### Frontend

The Breaker frontend runs on React.js using Redux and Immutable js. To install:

1. Install `npm` and `webpack`
2. `npm install && webpack`


### Backend

The Breaker backend runs on Java Play using PostgreSQL and Redis. To Install:

1. Install Postgres (see [Postgres](#postgres) below)
2. Install Redis (see [Redis](#redis) below)
3. Download Play 1.4.2: `https://downloads.typesafe.com/play/1.4.2/play-1.4.2.zip`
4. Add `play-1.4.2` directory to PATH
5. Run `play deps` in project directory.

### Configuration
There are a few configuration settings you need to set to run Breaker locally:
1. Update database credentials (see [Database](#database) below)
2. Copy .env.sample to .env and set appropriate values. None of them are strictly necessary to run a local server but things like the AWS keys are needed to upload profile pictures, etc.


# Database

The PostgreSQL database settings are in `application.conf`. To setup your PostgreSQL database (defaults included after colon):

`$ createdb <database_name:redditchat>`  
`$ psql <database_name:redditchat>`  
`psql$ CREATE ROLE <username:matt> superuser;`  
`psql$ CREATE USER <username:matt>;`  
`psql$ GRANT ROOT TO <username:matt>;`  
`psql$ GRANT ROOT TO <username:matt>;`  

You can now connect to the database at:

`postgres://<username:matt>@localhost/<database_name:redditchat>`

The schema will be created on page load. Note: you should set the default rooms to `open` in the `chatroom` table.

# Postgres via Brew instructions

If you've installed postgres via brew then you can probably control it via these commands:
brew services start postgres
brew services restart postgres

And if your machine has crashed it might have left a pid file behind and won't let you start the server, do this:
rm /usr/local/var/postgres/postmaster.pid
brew services start postgres

# Redis instructions

Install the play module:

play install redis
play deps

Hopefully the library dependency in the idea project points to the right place in your project so you can compile.

Now, install redis:

brew install redis
(this will show some info, or later you can run 'brew info redis')
launchctl load ~/Library/LaunchAgents/homebrew.mxcl.redis.plist
