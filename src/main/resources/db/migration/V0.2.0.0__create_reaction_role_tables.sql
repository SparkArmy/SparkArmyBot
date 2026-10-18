create table table_reaction_role
(
    "pk_rr_id"       bigserial    not null
        constraint table_reaction_role_id_pk
            primary key,
    "fk_rr_guild_id" bigint       not null
        constraint fk_table_reaction_role_fk_rr_guild_id__pk_gld_id
            references table_guild
            on update cascade
            on delete cascade,
    "rr_message_id"  bigint       not null,
    "rr_channel_id"  bigint       not null,
    "rr_role_id"     bigint       not null,
    "rr_emoji"       varchar(100) not null
);
