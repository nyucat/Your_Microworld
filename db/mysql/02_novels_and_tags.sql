USE your_microworld;

CREATE TABLE novel (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  author_id BIGINT UNSIGNED NOT NULL,
  description TEXT NULL,
  world_setting TEXT NULL,
  outline_content TEXT NULL,
  cover_url VARCHAR(500) NULL,
  allow_if_branch BOOLEAN NOT NULL DEFAULT TRUE,
  allow_bid BOOLEAN NOT NULL DEFAULT TRUE,
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_novel_author_created (author_id, created_at DESC),
  KEY idx_novel_status_created (status, created_at DESC),
  CONSTRAINT fk_novel_author FOREIGN KEY (author_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE tag (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(40) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_tag_name (name)
) ENGINE=InnoDB;

CREATE TABLE novel_tag (
  novel_id BIGINT UNSIGNED NOT NULL,
  tag_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (novel_id, tag_id),
  CONSTRAINT fk_novel_tag_novel FOREIGN KEY (novel_id) REFERENCES novel(id) ON DELETE CASCADE,
  CONSTRAINT fk_novel_tag_tag FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
) ENGINE=InnoDB;
