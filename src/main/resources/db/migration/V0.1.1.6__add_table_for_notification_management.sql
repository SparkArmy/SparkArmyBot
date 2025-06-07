create table table_content_creator
(
    pk_cct_id    varchar(500) not null
        constraint table_content_creator_pk
            primary key,
    cct_name     varchar(200) not null,
    cct_platform integer      not null
);

create table table_guild_notification_channel
(
    pk_fk_ncl_channel_id      bigint        not null
        constraint table_notification_channel_pk_fk_ncl_channel_id__channel_id_pk
            references table_guild_channel
            on update cascade on delete cascade,
    pk_fk_ncl_content_creator varchar(500)  not null
        constraint table_notification_channel_fk_ncl_content_creator__pk_cct_id
            references table_content_creator
            on update cascade on delete cascade,
    ncl_roles                 bigint[]      null,
    ncl_message               varchar(1000) null,

    constraint table_notification_channel_pk
        primary key (pk_fk_ncl_channel_id, pk_fk_ncl_content_creator)
);