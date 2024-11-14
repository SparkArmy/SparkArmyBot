create table table_persistent_views
(
    pk_pvs_id      bigint       not null
        primary key,
    pvs_data       bytea        not null,
    pvs_class_name varchar(100) not null
)