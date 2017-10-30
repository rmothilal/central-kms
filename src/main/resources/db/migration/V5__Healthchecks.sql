CREATE TABLE IF NOT EXISTS healthchecks (
    id uuid NOT NULL,
    sidecar_id UUID NOT NULL,
    level INT NOT NULL,
    created TIMESTAMPTZ NOT NULL,
    status INT NOT NULL,
    responded TIMESTAMPTZ,
    response TEXT,
    CONSTRAINT pk_healthcheck_id PRIMARY KEY (id),
    CONSTRAINT fk_healthchecks_sidecars FOREIGN KEY (sidecar_id)
        REFERENCES sidecars (id)
)