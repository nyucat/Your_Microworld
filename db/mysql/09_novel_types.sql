USE your_microworld;

ALTER TABLE novel
  ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'SERIAL' AFTER author_id,
  ADD COLUMN micro_content LONGTEXT NULL AFTER description;

UPDATE novel
SET type = 'SERIAL'
WHERE type IS NULL OR type = '';
