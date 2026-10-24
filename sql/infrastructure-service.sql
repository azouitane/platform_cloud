
CREATE TABLE clusters (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    api_url VARCHAR(500) NOT NULL,
    secret_ref VARCHAR(500) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE nodes (
    id UUID PRIMARY KEY,
    cluster_id UUID NOT NULL REFERENCES clusters(id),
    name VARCHAR(100) NOT NULL,
    status VARCHAR(30),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_node_name_per_cluster UNIQUE (cluster_id, name)
);
