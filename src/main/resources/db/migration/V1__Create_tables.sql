create table if not exists LINKS_TO_BE_PROCESSED
(
    link varchar(100)
);

create table if not exists LINKS_ALREADY_PROCESSED
(
    link varchar(100)
);

create table if not exists NEWS
(
    id          bigint primary key auto_increment,
    title       text,
    content     text,
    url         varchar(100) not null,
    created_at  timestamp default now(),
    modified_at timestamp default now()
);