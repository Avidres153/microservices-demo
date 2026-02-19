create table if not exists customer_snapshot(
	id bigint primary key,
	customer_name varchar(255) not null
);

create table if not exists account(
	id bigserial primary key,
	account_number varchar(10) unique not null,
	account_type varchar(20) not null,
	initial_balance numeric,
	status boolean,
	customer_id bigint not null,
	FOREIGN KEY (customer_id) REFERENCES customer_snapshot(id)
);

create table if not exists movement(
	id bigserial primary key,
	movement_date date not null,
	movement_type varchar(255) not null,
	value numeric not null,
	balance numeric not null,
	account_id bigint not null,
	FOREIGN KEY (account_id) REFERENCES account(id)
);