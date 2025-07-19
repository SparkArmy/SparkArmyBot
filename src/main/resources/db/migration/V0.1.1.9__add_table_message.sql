create table table_message
(
    pk_msg_id       bigint         not null
        constraint table_message_pk
            primary key,
    msg_content     varchar(10000) not null,
    fk_msg_channel  bigint         not null
        constraint table_message_fk_msg_channel_table_channel_pk_cnl_id
            references table_channel
            on update cascade on delete cascade,
    msg_attachments bytea[]        null,
    msg_last_update timestamp      not null
);

create table table_reaction_role_menu
(
    pk_fk_rrm_msg_id bigint not null
        constraint table_reaction_role_pk
            primary key
        constraint table_reaction_role_pk_fk_rrl_msg_id_table_message_pk_msg_id
            references table_message
            on update cascade on delete cascade,
    rrm_entries      json[] not null
)