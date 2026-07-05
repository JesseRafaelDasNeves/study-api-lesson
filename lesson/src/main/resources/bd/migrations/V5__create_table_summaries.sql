CREATE TABLE IF NOT EXISTS summaries (
    id UUID PRIMARY KEY,
    lesson_id UUID NOT NULL UNIQUE,
    content TEXT NOT NULL,
    source VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_summaries_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    CONSTRAINT chk_summaries_source CHECK (source IN ('UPLOADED_FILE', 'TOPIC', 'MANUAL'))
);
