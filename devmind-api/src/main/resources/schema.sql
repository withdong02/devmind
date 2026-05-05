-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- RAG: Documents table
CREATE TABLE IF NOT EXISTS documents (
    id VARCHAR(36) PRIMARY KEY,
    source_type VARCHAR(20) NOT NULL,
    source_path VARCHAR(1024),
    title VARCHAR(512) NOT NULL,
    content TEXT,
    language VARCHAR(50),
    file_hash VARCHAR(64),
    metadata_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_documents_source_path ON documents(source_path);
CREATE INDEX IF NOT EXISTS idx_documents_file_hash ON documents(file_hash);

-- RAG: Document chunks table with vector and full-text search
CREATE TABLE IF NOT EXISTS document_chunks (
    id VARCHAR(36) PRIMARY KEY,
    document_id VARCHAR(36) NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    chunk_type VARCHAR(20) NOT NULL,
    start_line INT,
    end_line INT,
    symbol_name VARCHAR(256),
    embedding vector(384),
    token_count INT,
    search_vector tsvector,
    metadata_json TEXT
);

CREATE INDEX IF NOT EXISTS idx_chunks_document_id ON document_chunks(document_id);
CREATE INDEX IF NOT EXISTS idx_chunks_search_vector ON document_chunks USING gin(search_vector);

-- ivfflat index requires existing data; create after initial indexing
-- CREATE INDEX IF NOT EXISTS idx_chunks_embedding ON document_chunks USING ivfflat (embedding vector_cosine_ops) WITH (lists = 10);

-- Memory: Long-term memories (user preferences, interaction summaries)
CREATE TABLE IF NOT EXISTS long_term_memories (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    memory_type VARCHAR(32) NOT NULL DEFAULT 'PREFERENCE',
    content TEXT NOT NULL,
    importance REAL NOT NULL DEFAULT 0.5,
    access_count INT NOT NULL DEFAULT 0,
    embedding vector(384),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_accessed TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ltm_user_id ON long_term_memories(user_id);
CREATE INDEX IF NOT EXISTS idx_ltm_memory_type ON long_term_memories(memory_type);

-- Memory: Episodic memories (past task records)
CREATE TABLE IF NOT EXISTS episodic_memories (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    task_description TEXT NOT NULL,
    steps_taken TEXT,
    outcome VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN',
    learnings TEXT,
    embedding vector(384),
    importance REAL NOT NULL DEFAULT 0.5,
    access_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_accessed TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_em_user_id ON episodic_memories(user_id);

-- Memory: Semantic knowledge (extracted facts)
CREATE TABLE IF NOT EXISTS semantic_knowledge (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    fact TEXT NOT NULL,
    source VARCHAR(512),
    confidence REAL NOT NULL DEFAULT 0.5,
    embedding vector(384),
    importance REAL NOT NULL DEFAULT 0.5,
    access_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_accessed TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sk_user_id ON semantic_knowledge(user_id);

-- Harness: Audit log
CREATE TABLE IF NOT EXISTS audit_log (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::varchar,
    user_id VARCHAR(64),
    action VARCHAR(64) NOT NULL,
    actor VARCHAR(64),
    target VARCHAR(256),
    input_data TEXT,
    output_data TEXT,
    success BOOLEAN NOT NULL DEFAULT true,
    error TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_user_id ON audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_log(action);
CREATE INDEX IF NOT EXISTS idx_audit_created_at ON audit_log(created_at);
