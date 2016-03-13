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