create table users(
	id bigserial primary key
	username text not null unique,
	password not null,
	last_money_update timestamp not null default now(),
	money double not null default 100,
	champion_time bigint not null default 0,
	waiting_for_fight_start timestamp,
	fights_win int not null 0,
	income double not null default 0,
	constraint lower_username_unique unique (lower(username))
);

create table animal_type(
	id smallserial primary key,
	name text not null unique
);

create table animal(
	user_id bigint bigint not null references users(id),
	animal_type_id smallint not null references animal_type(id),
	count int not null default 0,
	level int not null default 0,
	primary key(user_id, animal_type_id)
);

create table arena(
	id smallint primary key, 
	waiting_user_id bigint references users(id)
);

insert into arena(id) values(1);

