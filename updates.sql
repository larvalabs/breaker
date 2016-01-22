-- Admin flag
alter table chatuser add column admin boolean not null default false;

-- New messages storage
alter table userroom add column lastSeenMessageTime int8 default 0;

-- ALL DEPLOYED ABOVE