USE your_microworld;

-- parent_id 表示续写来源；MAIN 为可竞标主线，IF_BRANCH 为可继续分叉的平行线。
CREATE TABLE chapter (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  novel_id BIGINT UNSIGNED NOT NULL,
  parent_id BIGINT UNSIGNED NULL,
  author_id BIGINT UNSIGNED NOT NULL,
  type VARCHAR(20) NOT NULL,
  title VARCHAR(200) NOT NULL,
  content LONGTEXT NOT NULL,
  sequence_no INT UNSIGNED NOT NULL DEFAULT 1,
  branch_root_id BIGINT UNSIGNED NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  heat_score DECIMAL(14,2) NOT NULL DEFAULT 0,
  view_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  comment_count INT UNSIGNED NOT NULL DEFAULT 0,
  like_count INT UNSIGNED NOT NULL DEFAULT 0,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_chapter_novel_type_sequence (novel_id, type, sequence_no),
  KEY idx_chapter_parent (parent_id),
  KEY idx_chapter_author (author_id),
  KEY idx_chapter_heat (novel_id, heat_score DESC),
  CONSTRAINT fk_chapter_novel FOREIGN KEY (novel_id) REFERENCES novel(id) ON DELETE CASCADE,
  CONSTRAINT fk_chapter_parent FOREIGN KEY (parent_id) REFERENCES chapter(id) ON DELETE SET NULL,
  CONSTRAINT fk_chapter_author FOREIGN KEY (author_id) REFERENCES users(id),
  CONSTRAINT fk_chapter_branch_root FOREIGN KEY (branch_root_id) REFERENCES chapter(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 显式边便于前端结构图一次性查询，不必递归扫描 parent_id。
CREATE TABLE chapter_relation (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  novel_id BIGINT UNSIGNED NOT NULL,
  from_chapter_id BIGINT UNSIGNED NOT NULL,
  to_chapter_id BIGINT UNSIGNED NOT NULL,
  relation_type ENUM('MAIN', 'IF') NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_relation_edge (from_chapter_id, to_chapter_id),
  KEY idx_relation_novel (novel_id),
  CONSTRAINT fk_relation_novel FOREIGN KEY (novel_id) REFERENCES novel(id) ON DELETE CASCADE,
  CONSTRAINT fk_relation_from FOREIGN KEY (from_chapter_id) REFERENCES chapter(id) ON DELETE CASCADE,
  CONSTRAINT fk_relation_to FOREIGN KEY (to_chapter_id) REFERENCES chapter(id) ON DELETE CASCADE
) ENGINE=InnoDB;
