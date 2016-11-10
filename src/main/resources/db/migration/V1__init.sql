create table users(
	username text not null primary key,
	password text not null,
	last_money_update timestamp not null default now(),
	money double precision not null default 100,
	champion_time bigint not null default 0,
	waiting_for_fight_start timestamp,
	fights_win int not null default 0,
	income double precision not null default 0
);

create unique index lower_username_unique on users(lower(username));

create table animal(
	username text not null references users(username),
	animal_type text not null,
	count int not null default 0,
	level int not null default 0,
	primary key(username, animal_type)
);

create table arena(
	id smallint primary key, 
	waiting_user text references users(username)
);

insert into arena(id) values(1);

