CREATE TABLE IF NOT EXISTS inquiries (
    id UUID NOT NULL,
    service VARCHAR(1028) NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    created TIMESTAMPTZ NOT NULL,
    status INT NOT NULL,
    issued_to UUID NOT NULL,
    total INT NOT NULL,
    response_count INT NOT NULL,
    CONSTRAINT pk_inquiries_id PRIMARY KEY (id),
    CONSTRAINT fk_inquiries_sidecars FOREIGN KEY (issued_to)
        REFERENCES sidecars(id)
)
