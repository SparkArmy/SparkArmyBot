create table table_channel
(
    pk_cnl_id bigint       not null
        constraint table_channel_pk
            primary key,
    cnl_name  varchar(100) not null,
    cnl_type  integer      not null
);


create table table_guild_channel
(
    pk_fk_gcl_channel_id bigint not null
        constraint table_guild_channel_pk
            primary key
        constraint "table_guild_channel_table_channel_pk_fk_cnl_id_fk"
            references table_channel
            on UPDATE cascade on delete cascade,
    fk_gcl_guild_id      bigint not null
        constraint fk_table_guild_channel_fk_cnl_guild_id__pk_gld_id
            references table_guild
            on update cascade on delete cascade
);


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