create table table_reaction_role_menu
(
    pk_fk_rrm_msg_id bigint       not null
        constraint table_reaction_role_pk
            primary key
        constraint table_reaction_role_pk_fk_rrl_msg_id_table_message_pk_msg_id
            references table_message
            on update cascade on delete cascade,
    rrm_entries      json         not null,
    rrm_description  varchar(500) not null,
    fk_rrm_guild_id  bigint       not null
        constraint table_reaction_role_menu_table_guild_pk_gld_id_fk
            references bot.table_guild
            on update cascade on delete cascade
);