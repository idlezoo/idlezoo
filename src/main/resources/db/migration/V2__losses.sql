alter table users
add column fights_loss int not null default 0;

alter table users
add column perks int[] not null default '{}';

alter table animal
add column lost int not null default 0;

