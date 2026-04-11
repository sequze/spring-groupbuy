insert into roles(name) values ('USER') on conflict do nothing;
insert into roles(name) values ('ADMIN') on conflict do nothing;