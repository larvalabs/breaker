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

-- link bot room preferences
update chatroom set linkBotPref = 'newtop';

-- Chat room deleted flag
alter table chatroom add column deleted boolean DEFAULT false;

-- Manually added room mods join
CREATE TABLE user_moderatorroomchatonly
(
  moderatorschatonly_id BIGINT NOT NULL,
  moderatedroomschatonly_id BIGINT NOT NULL,
  CONSTRAINT user_moderatorroomchatonly_pkey PRIMARY KEY (moderatorschatonly_id, moderatedroomschatonly_id),
  CONSTRAINT fk_7w6eloi7oivos987rq37uciw1 FOREIGN KEY (moderatorschatonly_id) REFERENCES chatuser (id),
  CONSTRAINT fk_b7tu3cvox4k8qedygl07u1lte FOREIGN KEY (moderatedroomschatonly_id) REFERENCES chatroom (id)
);

-- ALL DEPLOYED ABOVE
