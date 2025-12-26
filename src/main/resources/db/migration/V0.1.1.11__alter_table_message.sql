-- Add column for message is deleted

alter table table_message
    add column msg_is_deleted BOOLEAN default false;