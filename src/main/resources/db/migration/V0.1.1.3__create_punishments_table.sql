create table "table_punishments"
(
    "psm_id"                   bigserial    not null primary key,
    "psm_type"                 smallint     not null,
    "fk_psm_offender_user_id"  bigint       not null
        constraint fk_table_punishments_fk_psm_offender_user_id__pk_usr_id
            references table_user
            on update cascade
            on delete cascade,
    "fk_psm_moderator_user_id" bigint       not null
        constraint fk_table_punishments_fk_psm_moderator_user_id__pk_usr_id
            references table_user
            on update cascade
            on delete cascade,
    "fk_psm_guild_id"          bigint       not null
        constraint fk_table_punishments_fk_psm_guild_id__pk_gld_id
            references table_guild
            on update cascade
            on delete cascade,
    "psm_reason"               varchar(512) not null
        constraint check_table_punishments_reason_in_range
            check (char_length((psm_reason)::text) >= 10 )
)