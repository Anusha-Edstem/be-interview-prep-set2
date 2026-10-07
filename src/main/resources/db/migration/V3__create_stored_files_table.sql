CREATE TABLE stored_files (
    id UUID PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(20) NOT NULL,
    size_bytes BIGINT NOT NULL,
    storage_key VARCHAR(100) NOT NULL,
    uploaded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_stored_files_storage_key UNIQUE (storage_key),
    CONSTRAINT ck_stored_files_size_positive CHECK (size_bytes > 0)
);

CREATE INDEX idx_stored_files_uploaded_at ON stored_files (uploaded_at);
