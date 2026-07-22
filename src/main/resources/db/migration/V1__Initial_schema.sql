CREATE TABLE teams (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE agents (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL REFERENCES teams(id),
    name VARCHAR(255) NOT NULL,
    max_chats INT NOT NULL,
    active_chats_count INT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_agent_capacity CHECK (active_chats_count <= max_chats)
);

CREATE TABLE chat_sessions (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL REFERENCES teams(id),
    agent_id UUID REFERENCES agents(id),
    customer_id VARCHAR(255) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    subject VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    queued_at TIMESTAMP WITH TIME ZONE,
    started_at TIMESTAMP WITH TIME ZONE,
    resolved_at TIMESTAMP WITH TIME ZONE,
    closed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE queue_entries (
    id UUID PRIMARY KEY,
    chat_id UUID NOT NULL REFERENCES chat_sessions(id) ON DELETE CASCADE,
    team_id UUID NOT NULL REFERENCES teams(id),
    entered_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Index for queue FIFO ordering
CREATE INDEX idx_queue_entries_team_entered ON queue_entries(team_id, entered_at);
