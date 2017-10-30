ALTER TABLE public."Keys" ADD COLUMN service_name VARCHAR(1028);

UPDATE public."Keys" SET service_name = id;

ALTER TABLE public."Keys" ALTER COLUMN service_name SET NOT NULL;