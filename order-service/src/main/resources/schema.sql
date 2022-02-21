create table orders(
    id bigint not null primary key auto_increment,
    product_id bigint not null,
    quantity int not null,
    name varchar(255) not null,
    description varchar(255) not null,
    status varchar(255) not null
);