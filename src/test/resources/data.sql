insert into my_domain values (1, 'attribute 1 rob');
insert into my_domain values (2, 'attribute 2 rob');

insert into my_domain values (10, 'attribute 1 luke');
insert into my_domain values (11, 'attribute 2 luke');


insert into permission (id,domain_type,domain_id, permission, username) values(0, 'demo.MyDomain', null, 'create', 'rob');
insert into permission (id,domain_type,domain_id, permission, username) values(1, 'demo.MyDomain', '1', 'read', 'rob');
insert into permission (id,domain_type,domain_id, permission, username) values(2, 'demo.MyDomain', '1', 'write', 'rob');

insert into permission (id,domain_type,domain_id, permission, username) values(20, 'demo.MyDomain', '2', 'read', 'rob');

-- make sure domain_type is honored
insert into permission (id,domain_type,domain_id, permission, username) values(100, 'demo.Other', '1', 'read', 'luke');