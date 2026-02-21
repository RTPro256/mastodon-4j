-- Federation audit logs for tracking incoming/outgoing federation activities
CREATE TABLE federation_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    direction VARCHAR(16) NOT NULL,
    event_type VARCHAR(64),
    actor_uri TEXT,
    target_uri TEXT,
    status VARCHAR(32),
    error TEXT
);

-- Index for querying by creation time
CREATE INDEX idx_federation_audit_logs_created_at ON federation_audit_logs(created_at);

-- Index for querying by direction (inbound/outbound)
CREATE INDEX idx_federation_audit_logs_direction ON federation_audit_logs(direction);

-- Index for querying by status
CREATE INDEX idx_federation_audit_logs_status ON federation_audit_logs(status);

-- Index for querying by event type
CREATE INDEX idx_federation_audit_logs_event_type ON federation_audit_logs(event_type);

COMMENT ON TABLE federation_audit_logs IS 'Audit log for federation activities (incoming/outgoing)';
COMMENT ON COLUMN federation_audit_logs.direction IS 'Direction of federation activity: inbound or outbound';
COMMENT ON COLUMN federation_audit_logs.event_type IS 'Type of ActivityPub event (Create, Update, Delete, Follow, etc.)';
COMMENT ON COLUMN federation_audit_logs.actor_uri IS 'URI of the actor that initiated the activity';
COMMENT ON COLUMN federation_audit_logs.target_uri IS 'URI of the target object of the activity';
COMMENT ON COLUMN federation_audit_logs.status IS 'Status of the federation activity: success, failed, pending';
COMMENT ON COLUMN federation_audit_logs.error IS 'Error message if status is failed';
