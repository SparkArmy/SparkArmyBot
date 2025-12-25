create table table_message
(
    pk_msg_id       bigint         not null
        constraint table_message_pk
            primary key,
    msg_content varchar(10000),
    fk_msg_channel  bigint         not null
        constraint table_message_fk_msg_channel_table_channel_pk_cnl_id
            references table_channel
            on update cascade on delete cascade,
    msg_attachments bytea[]        null,
    msg_last_update timestamp      not null
);