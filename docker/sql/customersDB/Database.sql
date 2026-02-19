create table if not exists customer(
	id BIGSERIAL primary key,
	name varchar(100) not null,
	gender varchar(10) not null,
	identification varchar(20) unique not null,
	address varchar(50) not null,
	phone varchar(20) not null,
	password varchar(16) not null,
	status boolean not null
);