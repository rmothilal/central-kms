CREATE TABLE IF NOT EXISTS batches (
  id uuid NOT NULL,
  sidecar_id uuid NOT NULL,
  signature text NOT NULL,
  "timestamp" TIMESTAMPTZ NOT NULL,
  CONSTRAINT pk_id PRIMARY KEY (id),
  CONSTRAINT fk_batches_keys FOREIGN KEY (sidecar_id)
      REFERENCES public."Keys" (id)
);