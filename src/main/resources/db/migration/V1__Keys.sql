CREATE TABLE IF NOT EXISTS public."Keys"
(
  "id" uuid NOT NULL,
  "public_key" VARCHAR(128) NOT NULL,
  CONSTRAINT "PK_ID" PRIMARY KEY (id)
)