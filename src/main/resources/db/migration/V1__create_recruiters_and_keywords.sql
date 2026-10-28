CREATE TABLE recruiters (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    position VARCHAR(160) NOT NULL,
    company VARCHAR(160) NOT NULL,
    location VARCHAR(160),
    linkedin_url VARCHAR(500) NOT NULL,
    headline VARCHAR(500),
    connections_count INTEGER,
    score INTEGER NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    invitation_sent_at TIMESTAMP,
    connected_at TIMESTAMP
);

CREATE TABLE keywords (
    id UUID PRIMARY KEY,
    description VARCHAR(160) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_recruiters_status ON recruiters (status);
CREATE INDEX idx_recruiters_score ON recruiters (score);
CREATE INDEX idx_recruiters_company ON recruiters (company);
CREATE INDEX idx_recruiters_created_at ON recruiters (created_at);
