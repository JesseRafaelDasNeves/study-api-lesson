CREATE TABLE IF NOT EXISTS summary_tags (
    id UUID PRIMARY KEY,
    summary_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_summary_tags_summary FOREIGN KEY (summary_id) REFERENCES summaries(id) ON DELETE CASCADE,
    CONSTRAINT fk_summary_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id)
);
