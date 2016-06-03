-- Admin flag
alter table chatuser add column admin boolean not null default false;

-- New messages storage
alter table userroom add column lastSeenMessageTime int8 default 0;

-- displayName official storage
alter table chatroom add COLUMN displayname VARCHAR(255);
update chatroom set displayname=name;
update chatroom set name=LOWER(displayname);

-- reddit user create date and suspended value
alter table chatuser add column redditUserCreatedUTC int8 default -1;
alter table chatuser add column redditUserSuspended boolean default false;

-- ALL DEPLOYED ABOVE

-- link bot room preferences
update chatroom set linkBotPref = 'newtop';