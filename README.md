# Your Microworld

一个面向协作式叙事的小说社区原型。当前版本已完成 Sprint 0、Sprint 1，并在此基础上完成了 Sprint 2 的评论与标签系统，同时保留了微小说 / 连载小说双类型发布能力与可爱化前端风格。

## 当前可用能力

- 账号注册 / 登录
- JWT 鉴权
- 首页、已发布页、个人主页
- 发布小说
  - 微小说：一次发布完成，直接展示完整正文
  - 连载小说：发布小说并创建第一章，后续可继续追加章节
- 小说列表 / 小说详情 / 章节阅读
- 阅读页左侧固定上一章 / 下一章导航
- 阅读页底部固定阅读进度条
- 段落评论（折叠展开）
- 小说标签展示
- 首页按分类 / 标签筛选

## 技术栈

- 后端：Java 17、Spring Boot 3、Spring Data JPA、MySQL 8
- 前端：Vue 3、Vite、Vue Router
- 认证：JWT

## Sprint 进度

| Sprint | 状态 | 说明 |
| --- | --- | --- |
| Sprint 0 | 已完成 | 后端工程、MySQL 连接、统一返回结构、JWT 登录、前端初始化、路由、API 封装、登录注册、`/health` |
| Sprint 1 | 已完成 | 小说发布、小说列表、小说详情、章节阅读、主线续写 |
| Sprint 1 补充 | 已完成 | 个人主页、前端中文化、可爱风 UI、交互动效、固定阅读导航、阅读进度条、微小说 / 连载小说类型拆分 |
| Sprint 2 | 已完成 | 段落评论、评论列表、小说标签、首页分类筛选、社区化阅读互动 |
| Sprint 3+ | 未开始 | IF 分支、竞标续写、角色系统、分析图谱、热度排行等 |

## 小说类型说明

### 1. 微小说（`MICRO`）

- 发布时直接提交完整正文
- 不创建章节
- 小说详情页直接阅读正文
- 不支持继续追加后续章节

### 2. 连载小说（`SERIAL`）

- 发布时需要填写第一章标题和正文
- 自动创建主线第一章
- 可在详情页继续发布后续章节
- 当前仅原作者可续写主线

## Sprint 2 功能说明

### 段落评论

- 每个章节正文按自然段拆分
- 每段下方都可以折叠 / 展开评论区
- 登录用户可对任意段落发表评论
- 评论列表按创建时间展示，形成基础社区互动感

### 标签与分类

- 发布小说时可填写多个标签，最多 5 个
- 小说卡片、小说详情页会展示标签
- 首页支持按标签筛选小说列表
- 便于后续扩展为更完整的分类体系

## 目录结构

```text
Your_Microworld/
├── src/                       # Spring Boot 后端
├── frontend/                  # Vue 前端
├── db/mysql/                  # MySQL 脚本
├── config/                    # 本地配置（如 application-local.yml）
├── introduce.txt              # 项目原始需求说明
└── README.md
```

## 数据库脚本

按顺序导入 `db/mysql/` 下脚本：

```text
00_create_database.sql
01_users.sql
02_novels_and_tags.sql
03_chapters_and_graph.sql
04_comments.sql
05_bidding.sql
06_characters.sql
07_analytics.sql
08_align_users_role.sql
09_novel_types.sql
```

说明：

- `02_novels_and_tags.sql`：包含小说、标签、小说标签关联表
- `04_comments.sql`：包含段落评论相关表
- `08_align_users_role.sql`：对齐旧版 `users.role` 字段
- `09_novel_types.sql`：新增小说类型字段 `type` 与微小说正文 `micro_content`

如果你已导入到 `08`，并且还没有支持“微小说 / 连载小说”字段，请继续导入：

```text
db/mysql/09_novel_types.sql
```

Sprint 2 本轮不需要额外新增 SQL，评论与标签所需表结构已在现有脚本中覆盖。

## 本地运行

### 1. 启动后端

确保本地已准备：

- JDK 17
- Maven 3.9+
- MySQL 8

默认读取：

- 数据库：`your_microworld`
- 端口：`8080`

数据库账号密码可放在：

- `config/application-local.yml`

启动命令：

```powershell
mvn spring-boot:run
```

### 2. 启动前端

确保本地已准备：

- Node.js 20+

启动命令：

```powershell
cd frontend
npm install
npm run dev
```

前端默认访问：

- `http://localhost:5173`

Vite 会将 `/api` 代理到后端 `8080`。

## 主要接口

### 基础与鉴权

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/health` | 健康检查 |
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录 |

### 小说

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/novels` | 小说列表，可带 `tag` 查询参数 |
| POST | `/api/novels` | 发布小说 |
| GET | `/api/novels/{id}` | 小说详情 |
| GET | `/api/novels/{id}/chapters` | 连载章节列表 |
| POST | `/api/novels/{id}/chapters` | 追加主线章节 |

### 章节、评论、标签、用户

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/chapters/{id}` | 单章阅读 |
| GET | `/api/chapters/{id}/comments` | 获取章节段落评论 |
| POST | `/api/chapters/{id}/comments` | 发表评论 |
| GET | `/api/tags` | 获取标签列表 |
| GET | `/api/users/{id}` | 用户个人主页 |

## 发布小说请求示例

### 微小说

```json
{
  "title": "雨夜便利店",
  "type": "MICRO",
  "description": "一个发生在深夜便利店里的短篇故事",
  "tags": ["治愈", "都市"],
  "microContent": "正文内容……",
  "worldSetting": "",
  "outlineContent": "",
  "allowIfBranch": false,
  "allowBid": false,
  "firstChapterTitle": "",
  "firstChapterContent": ""
}
```

### 连载小说

```json
{
  "title": "星港回声",
  "type": "SERIAL",
  "description": "一部持续展开的科幻连载",
  "tags": ["科幻", "悬疑", "群像"],
  "microContent": "",
  "worldSetting": "未来星港、失落航线、人工意识……",
  "outlineContent": "可公开的大纲内容",
  "allowIfBranch": true,
  "allowBid": true,
  "firstChapterTitle": "第一章：港口来信",
  "firstChapterContent": "第一章正文……"
}
```

## 前端现状

当前前端已完成一轮可爱风重构，并加入以下交互：

- 页面切换淡入淡出
- 首页翻书动画
- 发布页写字笔动画
- 登录 / 注册成功提示动画
- 按钮 / 卡片悬停粒子动画
- 阅读页左侧固定章节切换
- 阅读页底部固定阅读进度条
- 段落评论折叠展开
- 标签卡片与筛选芯片

## 当前限制

- 微小说暂不支持章节系统
- 连载小说当前只支持作者本人续写主线
- 评论目前为基础段落评论，尚未做楼中楼、点赞、热度排序
- 分类筛选当前基于标签实现，后续可扩展为独立分类体系
- IF 分支、竞标续写、角色系统、分析图谱仍属于后续 Sprint

## 下一步建议

优先建议进入 Sprint 3：

- IF 分支创作
- 竞标续写 / 排队机制
- 评论点赞与热度排行
- 作者作品数据看板
- 角色关系与世界观可视化
