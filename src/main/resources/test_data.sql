 insert into wallet (id, balance, status) values(1, 100.23,1);
 insert into wallet (id, balance, status) values(2, 10000,0);
 
insert into wallet_history (id, wallet_id, before, after, amount, ref, createtime, type) 
values('id1', 1, 0, 100.23, 100.23, 'ref1', '2016-10-24 20:30:40', 'FUNDIN');
insert into wallet_history (id, wallet_id, before, after, amount, ref, createtime, type) 
values('id2', 2, 0, 10000, 10000, 'ref2', '2016-10-25 20:30:40', 'FUNDIN');