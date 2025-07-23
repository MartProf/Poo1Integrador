select * from persona
select * from ciclodecine
select * from concierto
select * from concierto_artistas
select * from evento
select * from evento_responsables
select * from exposicion
select * from feria
select * from participante
select * from pelicula
select * from taller

SELECT conname
FROM pg_constraint
WHERE conrelid = 'taller'::regclass AND contype = 'u';

ALTER TABLE taller DROP CONSTRAINT ukj2k5v8ajdq18ndekdn11vxbq9;