create table table_guild
(
    pk_gld_id bigint not null
        constraint table_guild_id_pk
            primary key,
    gld_name  text   not null
        constraint check_table_guild_0
            check ((char_length(gld_name) >= 2) AND (char_length(gld_name) <= 100))
);

alter table table_guild
    owner to botuser;

create table table_user
(
    pk_usr_id    bigint           not null
        primary key,
    usr_username varchar(32)      not null
        constraint check_table_user_0
            check ((char_length((usr_username)::text) >= 2) AND (char_length((usr_username)::text) <= 32)),
    usr_flags    bigint default 0 not null
);

alter table table_user
    owner to botuser;


create table table_member
(
    pk_fk_mbr_user_id  bigint           not null
        constraint fk_table_member_pk_fk_mbr_user_id__pk_usr_id
            references table_user
            on update cascade on delete cascade,
    pk_fk_mbr_guild_id bigint           not null
        constraint fk_table_member_pk_fk_mbr_guild_id__pk_gld_id
            references table_guilds
            on update cascade on delete cascade,
    mbr_flags          bigint default 0 not null,
    constraint pk_table_member
        primary key (pk_fk_mbr_user_id, pk_fk_mbr_guild_id)
);

alter table table_member
    owner to botuser;
