USE your_microworld;

ALTER TABLE users
  ADD COLUMN bio TEXT NULL AFTER role;
