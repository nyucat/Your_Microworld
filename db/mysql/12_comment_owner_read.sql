USE your_microworld;

ALTER TABLE comment
  ADD COLUMN owner_read_at TIMESTAMP(6) NULL DEFAULT NULL AFTER updated_at;
