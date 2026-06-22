-- 已执行旧版 01_users.sql 的数据库：将 ENUM 角色列对齐到 JPA 实体的 VARCHAR(20)。
-- 对全新数据库执行同样安全。
USE your_microworld;

ALTER TABLE users
  MODIFY COLUMN role VARCHAR(20) NOT NULL DEFAULT 'READER';
