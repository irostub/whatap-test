create table product(
    id bigint not null primary key auto_increment,
    name varchar(255) not null,
    price integer not null,
    quantity integer default 0 not null
);