USE your_microworld;

-- 一次竞标针对 base_chapter 的下一章主线续写。
CREATE TABLE bid_round (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  novel_id BIGINT UNSIGNED NOT NULL,
  base_chapter_id BIGINT UNSIGNED NOT NULL,
  creator_id BIGINT UNSIGNED NOT NULL,
  deadline_at TIMESTAMP(6) NOT NULL,
  status ENUM('OPEN', 'CLOSED', 'CANCELLED', 'SETTLED') NOT NULL DEFAULT 'OPEN',
  winner_bid_id BIGINT UNSIGNED NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_bid_round_novel_status (novel_id, status, deadline_at),
  CONSTRAINT fk_bid_round_novel FOREIGN KEY (novel_id) REFERENCES novel(id) ON DELETE CASCADE,
  CONSTRAINT fk_bid_round_base FOREIGN KEY (base_chapter_id) REFERENCES chapter(id),
  CONSTRAINT fk_bid_round_creator FOREIGN KEY (creator_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE bid (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  bid_round_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  chapter_outline TEXT NOT NULL,
  writing_style VARCHAR(80) NOT NULL,
  impact_plan TEXT NOT NULL,
  status ENUM('PENDING', 'SELECTED', 'REJECTED', 'WITHDRAWN') NOT NULL DEFAULT 'PENDING',
  vote_count INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_bid_round_author (bid_round_id, user_id),
  KEY idx_bid_round_votes (bid_round_id, vote_count DESC),
  CONSTRAINT fk_bid_round FOREIGN KEY (bid_round_id) REFERENCES bid_round(id) ON DELETE CASCADE,
  CONSTRAINT fk_bid_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

ALTER TABLE bid_round
  ADD CONSTRAINT fk_bid_round_winner FOREIGN KEY (winner_bid_id) REFERENCES bid(id) ON DELETE SET NULL;

CREATE TABLE bid_vote (
  bid_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  vote TINYINT NOT NULL DEFAULT 1,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (bid_id, user_id),
  CONSTRAINT chk_bid_vote_value CHECK (vote IN (1, -1)),
  CONSTRAINT fk_bid_vote_bid FOREIGN KEY (bid_id) REFERENCES bid(id) ON DELETE CASCADE,
  CONSTRAINT fk_bid_vote_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
