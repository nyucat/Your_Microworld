USE your_microworld;

-- 日粒度快照：排行榜不必每次扫描阅读、评论、竞标明细。
CREATE TABLE chapter_daily_stat (
  chapter_id BIGINT UNSIGNED NOT NULL,
  stat_date DATE NOT NULL,
  view_count INT UNSIGNED NOT NULL DEFAULT 0,
  comment_count INT UNSIGNED NOT NULL DEFAULT 0,
  like_count INT UNSIGNED NOT NULL DEFAULT 0,
  bid_participation_count INT UNSIGNED NOT NULL DEFAULT 0,
  heat_score DECIMAL(14,2) NOT NULL DEFAULT 0,
  PRIMARY KEY (chapter_id, stat_date),
  KEY idx_daily_stat_heat (stat_date, heat_score DESC),
  CONSTRAINT fk_daily_stat_chapter FOREIGN KEY (chapter_id) REFERENCES chapter(id) ON DELETE CASCADE
) ENGINE=InnoDB;
