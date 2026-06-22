USE your_microworld;

CREATE TABLE character_profile (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  novel_id BIGINT UNSIGNED NOT NULL,
  name VARCHAR(100) NOT NULL,
  description TEXT NULL,
  attention_score DECIMAL(14,2) NOT NULL DEFAULT 0,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_character_novel_name (novel_id, name),
  KEY idx_character_attention (novel_id, attention_score DESC),
  CONSTRAINT fk_character_novel FOREIGN KEY (novel_id) REFERENCES novel(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE chapter_character (
  chapter_id BIGINT UNSIGNED NOT NULL,
  character_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (chapter_id, character_id),
  CONSTRAINT fk_chapter_character_chapter FOREIGN KEY (chapter_id) REFERENCES chapter(id) ON DELETE CASCADE,
  CONSTRAINT fk_chapter_character_character FOREIGN KEY (character_id) REFERENCES character_profile(id) ON DELETE CASCADE
) ENGINE=InnoDB;
