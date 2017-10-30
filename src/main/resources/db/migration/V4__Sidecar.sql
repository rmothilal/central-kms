CREATE TABLE IF NOT EXISTS public.sidecars
(
   id UUID not null,
   service_name VARCHAR(1028) not null,
   registered TIMESTAMPTZ not null,
   terminated TIMESTAMPTZ null,
   CONSTRAINT pk_sidecard_id PRIMARY KEY (id)
);

INSERT INTO sidecars (id, service_name, registered, terminated)
SELECT id, service_name, now() at time zone 'UTC', now() at time zone 'UTC'
FROM "Keys";

ALTER TABLE public."Keys" DROP COLUMN service_name;

ALTER TABLE public.batches DROP CONSTRAINT fk_batches_keys;

ALTER TABLE public.batches
  ADD CONSTRAINT fk_batches_sidecars FOREIGN KEY (sidecar_id)
      REFERENCES public.sidecars (id);
