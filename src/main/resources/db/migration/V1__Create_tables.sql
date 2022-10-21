create table if not exists LINKS_TO_BE_PROCESSED
(
    link varchar(200)
);

create table if not exists LINKS_ALREADY_PROCESSED
(
    link varchar(200)
);

create table if not exists NEWS
(
    id          bigint primary key auto_increment,
    title       text,
    content     text,
    url         varchar(200) not null,
    created_at  timestamp default now(),
    modified_at timestamp default now()
);