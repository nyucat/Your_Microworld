USE your_microworld;

ALTER TABLE novel
  ADD COLUMN category VARCHAR(40) NULL AFTER description;

UPDATE novel
SET category = '其他'
WHERE category IS NULL;
