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

alter table table_guild
    owner to botuser;

create table table_user
(
    "pk_usr_id"       bigint      not null
        primary key,
    "usr_username"    varchar(32) not null
        constraint check_table_user_name_in_range
            check ((char_length((usr_username)::text) >= 2) AND (char_length((usr_username)::text) <= 32)),
    "usr_displayname" varchar(32)
        constraint check_table_user_nickname_in_range
            check ((char_length(usr_displayname)::text) >= 1 AND (char_length((usr_displayname)::text) <= 32)),
    "usr_avatar"      text,
    "usr_banner"      text,
    "usr_flags"       bigint      not null default 0
);

alter table table_user
    owner to botuser;


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

alter table table_member
    owner to botuser;


create table table_guild_commands
(
    "pk_gcd_guild"      bigint       not null,
    "pk_gcd_identifier" varchar(100) not null,
    "gcd_hash"          char(64)     not null,
    constraint pk_table_guild_commands
        primary key (pk_gcd_guild, pk_gcd_identifier)
);

alter table table_guild_commands
    owner to botuser;


create table table_command_hashes
(
    pk_cmh_identifier varchar(100) not null
        primary key,
    cmh_hash          char(64)     not null
);

alter table table_command_hashes
    owner to botuser;