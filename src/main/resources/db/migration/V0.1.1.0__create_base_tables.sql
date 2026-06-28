create table table_guild
(
    "pk_gld_id"    bigint   not null
        constraint table_guild_id_pk
            primary key,
    "gld_name"     text     not null
        constraint check_table_guild_name_in_range
            check ((char_length(gld_name) >= 2) AND (char_length(gld_name) <= 100)),
    "gld_icon"     text,
    "gld_owner_id" bigint   not null,
    "gld_flags"    SMALLINT not null default 0,
    "gld_features" int
);


create table table_user
(
    "pk_usr_id"       bigint      not null
        primary key,
    "usr_username"    varchar(32) not null
        constraint check_table_user_name_in_range
            check ((char_length((usr_username)::text) >= 2) AND (char_length((usr_username)::text) <= 32)),
    "usr_displayname" varchar(32)
        constraint check_table_user_nickname_in_range
            check ((char_length((usr_displayname)::text) >= 1) AND (char_length((usr_displayname)::text) <= 32)),
    "usr_avatar"      text,
    "usr_banner"      text,
    "usr_flags"       bigint      not null default 0
);

create table table_bot_status
(
    "pk_bts_id"    bigserial    not null
        primary key,
    "bts_status"   varchar(128) not null
        constraint check_table_bot_status_in_range
            check ( (char_length((bts_status)::text) >= 1) AND (char_length((bts_status)::text) <= 128) ),
    "bts_activity" bigint       not null default 0
);


create table table_member
(
    "pk_fk_mbr_user_id"  bigint not null
        constraint fk_table_member_pk_fk_mbr_user_id__pk_usr_id
            references table_user
            on update cascade
            on delete cascade,
    "pk_fk_mbr_guild_id" bigint not null
        constraint fk_table_member_pk_fk_mbr_guild_id__pk_gld_id
            references table_guild
            on update cascade
            on delete cascade,
    "mbr_flags"          bigint not null default 0,
    constraint pk_table_member
        primary key (pk_fk_mbr_user_id, pk_fk_mbr_guild_id)
);