alter table users
add column perk_income double precision not null default 0;

alter table users
rename column income to base_income;