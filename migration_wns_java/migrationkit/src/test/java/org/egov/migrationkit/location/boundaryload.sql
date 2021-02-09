

--insert into  tmp_boundary (digitname,digitcode) values ( 
--=CONCATENATE("'",TRIM(AE1),"',")
--=CONCATENATE("'",TRIM(AJ1),"'")
--);

set search_path to sunam;
create table if not exists tmp_boundary (digitname varchar(1000),digitcode varchar(64),erpname  varchar(1000),erpcode varchar(64)
,loc varchar(150),block varchar(50)
);
\i sunam_boundary.sql


--select code,name from eg_boundary where upper(name) in (select upper(substr(digitname,1,position('-' in digitname)-2)) from tmp_boundary);


update  tmp_boundary set loc=split_part(digitname,' - ',1) ,block=split_part(digitname,' - ',2);

update  tmp_boundary set block=replace(block,'Ward','B');


update  tmp_boundary set block=replace(block,'W','B');

update  tmp_boundary set block=replace(block,' ','');
update  tmp_boundary set block=replace(block,'-','');


create table finallocation as
select digitcode,loca.code from eg_boundary loca,eg_boundary block,tmp_boundary b where loca.parent=block.id
and loca.name like '%'||b.loc||'%' and block.code=b.block ;



