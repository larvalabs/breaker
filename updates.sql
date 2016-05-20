-- Admin flag
alter table chatuser add column admin boolean not null default false;

-- New messages storage
alter table userroom add column lastSeenMessageTime int8 default 0;

-- displayName official storage
alter table chatroom add COLUMN displayname VARCHAR(255);
update chatroom set displayname=name;
update chatroom set name=LOWER(displayname);

-- ALL DEPLOYED ABOVE
