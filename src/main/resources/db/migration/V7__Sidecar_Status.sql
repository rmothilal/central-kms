ALTER TABLE public.sidecars
	ADD COLUMN status integer NULL,
	ADD COLUMN challenge VARCHAR(128) NULL;

UPDATE sidecars
	SET challenge = md5(random()::text || clock_timestamp()::text)::uuid,
	status = 1;

ALTER TABLE public.sidecars
	ALTER COLUMN status SET NOT NULL,
	ALTER COLUMN challenge SET NOT NULL;

UPDATE sidecars
	SET status = 3
	WHERE terminated is not null;

INSERT INTO sidecarlogs
	SELECT md5(random()::text || clock_timestamp()::text)::uuid, id, 1, registered
	FROM sidecars;

INSERT INTO sidecarlogs
	SELECT md5(random()::text || clock_timestamp()::text)::uuid, id, 4, terminated
	FROM sidecars
	WHERE terminated is not null;

ALTER TABLE sidecars
	DROP COLUMN registered,
	DROP COLUMN terminated;