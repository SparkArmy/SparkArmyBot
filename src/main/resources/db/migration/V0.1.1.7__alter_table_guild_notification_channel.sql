alter table table_guild_notification_channel
    add ncl_webhook_url varchar(500) not null,
    add ncl_last_time   timestamp    null;