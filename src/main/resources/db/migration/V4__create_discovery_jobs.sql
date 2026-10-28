CREATE TABLE discovery_jobs (
    id UUID PRIMARY KEY,
    requested_profiles INTEGER NOT NULL,
    profiles_found INTEGER NOT NULL,
    profiles_imported INTEGER NOT NULL,
    duplicates_ignored INTEGER NOT NULL,
    execution_time_ms BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    error_message VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP
);

CREATE INDEX idx_discovery_jobs_status ON discovery_jobs (status);
CREATE INDEX idx_discovery_jobs_created_at ON discovery_jobs (created_at);
