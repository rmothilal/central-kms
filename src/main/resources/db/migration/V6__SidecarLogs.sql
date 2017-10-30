CREATE TABLE IF NOT EXISTS sidecarlogs (
    id uuid NOT NULL,
    sidecar_id UUID NOT NULL,
    status INT NOT NULL,
    "timestamp" TIMESTAMPTZ NOT NULL,
    message TEXT NULL,
    CONSTRAINT pk_sidecarlog_id PRIMARY KEY (id),
    CONSTRAINT fk_sidecarlogs_sidecars FOREIGN KEY (sidecar_id)
        REFERENCES sidecars (id)
)
