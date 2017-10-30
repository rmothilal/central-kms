CREATE TABLE IF NOT EXISTS inquiry_responses (
    id UUID NOT NULL,
    inquiry_id UUID NOT NULL,
    batch_id UUID NOT NULL,
    "body" TEXT NOT NULL,
    item INT NOT NULL,
    created TIMESTAMPTZ NOT NULL,
    fulfilling_sidecar_id UUID NOT NULL,
    verified BOOL NOT NULL,
    error_message VARCHAR(1024) NULL,
    CONSTRAINT pk_inquiry_responses_id PRIMARY KEY (id)
);

CREATE INDEX ix_inquiry_responses_inquiry_id
    ON inquiry_responses
    USING btree(inquiry_id);
