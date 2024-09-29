create table table_guild_channel
(
    pk_cnl_id       bigint            not null
        constraint table_guild_channel_pk
            primary key,
    fk_cnl_guild_id bigint            not null
        constraint fk_table_guild_channel_fk_cnl_guild_id__pk_gld_id
            references table_guild
            on update cascade on delete cascade,
    cnl_name        varchar(100)      not null,
    cnl_type        integer default 0 not null
);

alter table table_guild_channel
    owner to botuser;

create table "table_log_guild_channel"
(
    pk_fk_lcn_id    bigint  not null
        constraint "table_log_guild_channel_pk"
            primary key
        constraint "table_log_guild_channel_table_guild_channel_pk_cnl_id_fk"
            references table_guild_channel
            on update cascade on delete cascade,
    lcn_webhook_url text    not null,
    lcn_type        integer not null
);

alter table "table_log_guild_channel"
    owner to botuser;