insert into my_domain values (1, 'attribute 1 rob');
insert into my_domain values (2, 'attribute 2 rob');

insert into my_domain values (10, 'attribute 1 luke');
insert into my_domain values (11, 'attribute 2 luke');

insert into acl_class values (1, 'demo.MyDomain');
insert into acl_class values (2, 'demo.Other');

insert into acl_sid values (1, 1, 'rob');
insert into acl_sid values (2, 1, 'luke');

insert into acl_object_identity (id, object_id_class, object_id_identity, parent_object, owner_sid, entries_inheriting)
                         values (1, 1, 1, null, 1, 1);
insert into acl_object_identity (id, object_id_class, object_id_identity, parent_object, owner_sid, entries_inheriting)
                         values (2, 1, 2, null, 1, 1);
insert into acl_object_identity (id, object_id_class, object_id_identity, parent_object, owner_sid, entries_inheriting)
                         values (3, 2, 1, null, 1, 1);

insert into acl_entry (id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
               values (1, 1, 0, 1, 7, 1, 0, 0);

insert into acl_entry (id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
               values (2, 2, 0, 1, 1, 1, 0, 0);

insert into acl_entry (id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
               values (3, 3, 0, 2, 1, 1, 0, 0);