drop table if exists MOVIE;
create table MOVIE (
    id varchar(255) not null,
    vector double precision array,
    primary key (id)
);
