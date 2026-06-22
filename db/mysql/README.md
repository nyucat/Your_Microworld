# MySQL 初始化脚本

适用于 MySQL 8.0+，请按文件编号顺序导入。每个文件只负责一个领域，后续变更请新建更高编号的迁移脚本，不修改已执行的脚本。

```powershell
Get-ChildItem .\db\mysql\*.sql | Sort-Object Name | ForEach-Object {
  Get-Content $_.FullName | mysql -u root -p
}
```

也可在 MySQL 客户端中逐个执行。导入顺序：数据库 → 用户 → 小说/标签 → 章节图 → 评论 → 竞标 → 角色 → 热度。

| 文件 | 领域 |
| --- | --- |
| `00_create_database.sql` | 数据库与字符集 |
| `01_users.sql` | 用户账户 |
| `02_novels_and_tags.sql` | 小说与标签 |
| `03_chapters_and_graph.sql` | 章节、主线、IF 线、图关系 |
| `04_comments.sql` | 段落评论、折叠回复、点赞 |
| `05_bidding.sql` | 竞标轮次、提案、投票 |
| `06_characters.sql` | 角色与章节关联 |
| `07_analytics.sql` | 章节热度日统计 |
| `08_align_users_role.sql` | 修复旧版 `users.role` 的类型不匹配 |
