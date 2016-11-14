create table users(
	id serial primary key,
	username text not null,
	password text not null,
	last_money_update timestamp not null default now(),
	money double precision not null default 50,
	champion_time bigint not null default 0,
	waiting_for_fight_start timestamp,
	fights_win int not null default 0,
	income double precision not null default 0
);

create unique index lower_username_unique on users(lower(username));

create table animal(
	user_id int not null references users(id),
	animal_type int not null,
	count int not null default 0,
	level int not null default 0,
	primary key(user_id, animal_type)
);

create table arena(
	id smallint primary key, 
	waiting_user_id int references users(id)
);

insert into arena(id) values(1);

