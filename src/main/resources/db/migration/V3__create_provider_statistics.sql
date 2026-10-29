CREATE TABLE provider_statistics (
    id UUID PRIMARY KEY,
    provider_name VARCHAR(80) NOT NULL UNIQUE,
    executions BIGINT NOT NULL,
    successful_executions BIGINT NOT NULL,
    failed_executions BIGINT NOT NULL,
    candidates_found BIGINT NOT NULL,
    candidates_imported BIGINT NOT NULL,
    duplicates_ignored BIGINT NOT NULL,
    total_execution_time_ms BIGINT NOT NULL,
    total_score DOUBLE PRECISION NOT NULL,
    score_samples BIGINT NOT NULL,
    average_execution_time_ms DOUBLE PRECISION NOT NULL,
    average_score DOUBLE PRECISION NOT NULL,
    last_execution_at TIMESTAMP
);

CREATE UNIQUE INDEX idx_provider_statistics_provider ON provider_statistics (provider_name);
CREATE INDEX idx_provider_statistics_last_execution ON provider_statistics (last_execution_at);
