create table "table_guild_punishment_config"
(
    "pk_fk_gco_guild_id" bigint not null primary key
        constraint fk_table_guild_punishment_config_pk_fk_gco_guild_id__pk_gld_id
            references table_guild
            on update cascade
            on delete cascade,
    "gco_mute_role_id"   bigint,
    "gco_warn_role_id"   bigint

)