
--Crearea de tabele
--create table Utilizator( 
--id_u serial primary key, 
--nume varchar(50) not null, 
--adresa varchar(100), 
--data_nasterii date not null, 
--telefon int not null);


--create table articol( 
--id_a serial primary key, 
--denumire varchar(50) not null, 
--categorie varchar(50) not null, 
--stoc int not null, 
--pret int not null, 
--discount int, 
--rating int); 

--create table ComandaOnline( 
--id_co int primary key, 
--data_comanda date not null, 
--id_u int not null, 
--suma_totala int , 
--status text not null, 
--metoda_plata varchar(50) not null, 
--foreign key(id_u) references Utilizator(id_u));
--
--create table DetaliuComanda( 
--id_co int not null, 
--id_a int not null, 
--cantitate int not null, 
--pret_final int, 
--foreign key(id_co) references ComandaOnline(id_co), 
--foreign key(id_a) references Articol(id_a)); 
--
--create table CategorieArticol( 
--id_cat serial primary key, 
--nume_categorie varchar(100) not null, 
--descriere text 
--);

--alter table Utilizator add column email varchar(100);

--2.a)
--alter table Utilizator 
--add constraint ck_utizator_peste18 
--check(extract(year from age(current_date,data_nasterii))>=18);

--2.b)
--alter table ComandaOnline 
--add constraint ck_plata_card_minima 
--check ( metoda_plata <> 'card' or suma_totala >=20) ;

--3.a)
--select * from ComandaOnline 
--where data_comanda>= current_date - 30 
--order by data_comanda;

--3.b)
--select * 
--from articol 
--where pret>200 
--order by denumire asc,categorie desc;

--4.a)
--select c.id_co, nume, denumire, data_comanda 
--from Utilizator u 
--left join ComandaOnline c on u.id_u = c.id_u 
--join DetaliuComanda d on d.id_co = c.id_co 
--join Articol a on d.id_a = a.id_a 
--where extract(year from age(current_date,u.data_nasterii))>25;

--4.b)
--select a1.id_a as id_articol1, a2.id_a as id_articol2
--from DetaliuComanda d1
--join DetaliuComanda d2 
--    on d1.id_co = d2.id_co 
--   and d1.id_a < d2.id_a
--join Articol a1 on d1.id_a = a1.id_a
--join Articol a2 on d2.id_a = a2.id_a
--where  a1.categorie <> a2.categorie;

--5.a)
--select co.*
--from ComandaOnline co 
--where exists(
--	select 1
--	from DetaliuComanda d 
--	join articol a on d.id_a =a.id_a
--	where d.id_co = co.id_co and rating < 3
--);

--5.b)
--select co.*
--from ComandaOnline co 
--join Utilizator u on co.id_u = u.id_u
--where (current_date - u.data_nasterii )> all(
--	select(current_date-u2.data_nasterii) 
--	from utilizator u2
--	where u2.id_u in(
--		select id_u
--		from ComandaOnline
--		where data_comanda>='2025-01-01' and data_comanda<'2025-02-01')
--		);	

--6.a)
--select u.nume,avg(c.suma_totala) as val_medie
--from Utilizator u
--join ComandaOnline c on c.id_u = u.id_u 
--where c.data_comanda >='2025-01-01' and c.data_comanda <'2026-01-01'
--group by u.nume ;

--6.b)
--select a.denumire,count(distinct d.id_co) as numar_comenzi,sum(d.cantitate) as cantite_totala
--from articol a 
--join DetaliuComanda d on a.id_a = d.id_a 
--join ComandaOnline c on d.id_co = c.id_co 
--where c.data_comanda >='2025-01-01' and c.data_comanda <'2026-01-01'
--group by a.denumire ;

--7.a)
--insert into Utilizator(nume, adresa, data_nasterii, telefon, email) values
--('Radu Georgescu', 'Strada Brasov nr. 5', '1988-07-20', 0766123, 'radu.georgescu@gmail.com'),
--('Maria Dumitrescu', 'Aleea Rozelor nr. 12', '1996-12-05', 0733456, 'elena.dumitrescu@gmail.com'),
--('Vlad Ionescu', 'Bulevardul Timisoara 55', '1982-03-30', 0722789, 'vlad.ionescu@gmail.com'),
--('Popescu Alex', 'Albac 105 ', '2005-01-01', 0755333, 'alex@gmail.com');

--insert into ComandaOnline(id_co,id_u,data_comanda,suma_totala,status,metoda_plata) 
--values (500,1,'2025-11-20',450,'in procesare','card');

--7.b)
--delete from comandaonline 
--where suma_totala<50 and status='anulata';

--7.c)
--update comandaonline c 
--set suma_totala = (
--	select sum(d.pret_final*d.cantitate) 
--	from detaliucomanda d 
--	where d.id_co = c.id_co)
--	where c.id_co in (
--	select c2.id_co 
--	from comandaonline c2
--	join utilizator u on c2.id_u = u.id_u 
--	where extract(year from age(current_date,u.data_nasterii)) > 30 
--	);

--triggere punctul 8 

--create or replace function trg_update()
--returns trigger as $$
--begin
--    if tg_op = 'insert' then
--        update articol
--        set stoc = stoc - new.cantitate
--        where id_a = new.id_a;
--
--        if (select stoc from articol where id_a = new.id_a) < 0 then
--            raise exception 'stoc insuficient';
--        end if;
--
--        return new;--practic continua operatia daca totul e ok si insereaza sau updateaza
--
--    elsif tg_op = 'update' then
--        update articol
--        set stoc = stoc - (new.cantitate - old.cantitate)
--        where id_a = new.id_a;
--
--        if (select stoc from articol where id_a = new.id_a) < 0 then
--            raise exception 'stoc insuficient dupa modificare';
--        end if;
--
--        return new;
--
--    elsif tg_op = 'delete' then
--        update articol
--        set stoc = stoc + old.cantitate
--        where id_a = old.id_a;
--
--        return old;--practic sterge ce a fost inainte
--    end if;
--
--    return null;
--end;
--$$ language plpgsql;

--create or replace function trg_no_stoc()
--returns trigger as $$
--begin
--    if (select stoc from articol where id_a = new.id_a) <= 0 then
--        raise exception 'nu se poate adauga, stoc = 0';
--    end if;
--    return new;
--end;
--$$ language plpgsql;


--create trigger trg_check_stop
--before insert on detaliucomanda
--for each row
--execute function trg_no_stoc();
--
--CREATE VIEW ComandaDetaliata500 AS
--SELECT co.id_co, co.data_comanda, co.suma_totala, co.status,
-- u.nume AS nume_utilizator, u.telefon, u.data_nasterii,
-- a.denumire AS denumire_articol, a.categorie, dc.cantitate, dc.pret_final
--FROM Utilizator u
--JOIN ComandaOnline co ON co.id_u = u.id_u
--JOIN DetaliuComanda dc ON dc.id_co = co.id_co
--JOIN Articol a ON a.id_a = dc.id_a
--WHERE co.id_co = 500;

--create or replace function trg_insert_view()
--returns trigger as $$
--begin 
--	--pentru a insera doar daca nu exista deja
--	if not exists(
--		select 1 from Utilizator where nume = new.nume)
--	then 
--		insert into Utilizator(nume,telefon,data_nasterii)
--		values (new.nume,new.telefon,new.data_nasterii);
--	end if;
--	
--	--insersam comanda 500 doar daca nu exista
--	if not exists
--	(select 1 from ComandaOnline where id_co = 500)
--	then
--		insert into ComandaOnline(id_co,id_u,data_comanda,suma_totala,metoda_plata) 
--		values (new.id_co,new.id_u,new.data_comanda,new.suma_totala,new.metoda_plata);
--	end if;
--	
--	--inseram articolul doar daca nu exista deja unul cu aceeasi denum
--	if not exists
--	(select 1 from articol where denumire = new.denumire)
--	then 
--		insert into articol(denumire,categorie,stoc,pret)
--		values(new.denumire,new.categorie,new.stoc,new.pret);
--	end if;
--	--insert cantitate+ pret final 
--	
--	insert into DetaliuComanda(id_co,id_a,cantitate,pret_final)
--	values (500,(select id_a from articol where denumire = new.denumire limit 1),new.cantitate,new.pret_final);
--	
--	return new;
--	end;
--	$$ language plpgsql;

--create trigger trg_insertview500 
--instead of insert on ComandaDetaliata500
--for each row 
--execute function trg_insert_view();

--alter table articol alter column discount set default 0;

--create or replace function aplica_discount_comanda(
--    p_id_co int,
--    p_discount int
--)
--returns void as $$
--begin
--    -- actualizam suma totala aplicand discountul
--    update comandaonline
--    set suma_totala =
--        round(suma_totala * (1 - p_discount / 100.0))::int
--    where id_co = p_id_co;
--end;
--$$ language plpgsql;


--Inserari 
--insert into utilizator (nume, adresa, data_nasterii, telefon, email) values
--('ion pop', 'a', '1970-01-01', 700001, 'ion@mail.com'),
--('ana matei', 'b', '1985-06-10', 700002, 'ana@mail.com'),
--('mihai vasile', 'c', '1995-03-20', 700003, 'mihai@mail.com'),
--('elena radu', 'd', '1998-08-15', 700004, 'elena@mail.com'),
--('george mihai', 'e', '1960-02-02', 700005, 'george@mail.com'),
--('alex andrei', 'f', '1950-01-01', 700006, 'alex@mail.com');

--insert into categoriearticol (nume_categorie, descriere) values
--('fotbal', 'articole fotbal'),
--('fitness', 'articole fitness'),
--('tenis', 'articole tenis'),
--('imbracaminte', 'articole imbracaminte');

--insert into articol (denumire, categorie, stoc, pret, discount, rating) values
--('minge fotbal', 'fotbal', 50, 150, 0, 5),
--('gantere', 'fitness', 30, 250, 10, 2),
--('racheta tenis', 'tenis', 20, 600, 5, 4),
--('tricou sport', 'imbracaminte', 100, 80, 0, 2),
--('bicicleta fitness', 'fitness', 10, 1200, 15, 5),
--('aparat forta', 'fitness', 5, 900, 10, 2);

--insert into comandaonline (id_co, data_comanda, id_u, suma_totala, status, metoda_plata) values
--(1, '2025-01-05', 1, 300, 'livrata', 'card'),
--(2, '2025-01-15', 2, 250, 'livrata', 'ramburs'),
--(3, '2025-01-25', 3, 400, 'livrata', 'card'),
--(4, current_date - 5, 4, 600, 'livrata', 'card'),
--(5, current_date - 10, 5, 450, 'in procesare', 'ramburs'),
--(6, current_date - 20, 6, 700, 'livrata', 'card'),
--(7, '2025-03-10', 1, 500, 'livrata', 'card'),
--(8, '2025-03-12', 5, 650, 'livrata', 'card'),
--(9, '2025-03-15', 6, 800, 'livrata', 'card');

--insert into detaliucomanda (id_co, id_a, cantitate, pret_final) values
--(1, 1, 2, 150),
--(1, 2, 1, 250),
--(2, 3, 1, 600),
--(2, 4, 2, 80),
--(3, 5, 1, 1200),
--(4, 2, 2, 250),
--(4, 6, 1, 900),
--(5, 4, 3, 80),
--(6, 6, 1, 900),
--(7, 3, 1, 600),
--(7, 4, 1, 80),
--(8, 5, 1, 1200),
--(9, 2, 2, 250);

