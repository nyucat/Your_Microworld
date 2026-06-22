# Your Microworld

一个让故事以“图结构”生长的协作叙事社区。它不只是发布和阅读小说：主线可以由社区竞标续写，任一章节可衍生不同选择的 IF 线，读者还可在段落级参与讨论。

## 产品概念

一部小说由章节节点和关系边构成，而非只能线性阅读：

- **主作者**创建小说、世界观、标签和协作规则，并发布主线起点。
- **副作者**为主线下一章提交大纲、风格与剧情影响计划；社区在限时投票后决定创作权。
- **从作者**从某章创建 IF 线；IF 线不能竞标续写，但可继续向下分叉。
- **读者**阅读、段落评论、投票，并在结构图中探索平行叙事。

核心模型：`Novel → Chapter（MAIN / IF_BRANCH）→ ChapterRelation`。章节的 `parentId` 与关系表共同描述叙事树/图；评论绑定 `chapterId + paragraphIndex`。

## 迭代计划与验收版本

| Sprint | 范围 | 可演示成果 |
| --- | --- | --- |
| 0（已完成） | 工程基础、账户、健康检查 | 注册、登录、首页占位接口、MySQL 持久化数据库 |
| 1 | 小说与主线章节 | 创建小说、发布首章、列表与阅读 |
| 2 | 段落评论、标签 | 折叠评论、标签分类与筛选 |
| 3 | IF 线与章节树 | 从章节分叉、浏览不同故事线 |
| 4 | 章节竞标 | 提案、限时投票、获胜者续写主线 |
| 5 | 图谱与热度分析 | 结构图、热门章节/角色/分支排行 |

## 当前可运行版本（Sprint 0）

后端使用 Java 17、Spring Boot 3、Spring Data JPA 与 MySQL 8；前端位于 `frontend/`，采用 Vue 3 + Vite 与 Vue Router。注册/登录使用 BCrypt 校验密码，成功后返回 JWT；前端将令牌保存在本地并自动附加到后续 API 请求。接口统一 JSON 返回并具备参数校验。

### Sprint 0 验收状态

| 项目 | 状态 | 说明 |
| --- | --- | --- |
| Spring Boot 工程与统一响应 | 已完成 | Maven 可成功打包。 |
| MySQL 驱动与连接配置 | 已完成 | 已实测建立 JDBC 连接。 |
| 用户注册、登录 | 已完成 | 用户名唯一、参数校验、BCrypt 密码哈希。 |
| JWT 认证 | 已完成 | 登录/注册签发有效期 120 分钟的 JWT；`/api/auth/me` 已验证受保护访问。 |
| Vue 初始化、路由与 API 封装 | 已完成 | `/`、`/login`、`/register` 路由；`npm run build` 已通过，开发服务器代理 `/api`。 |
| Vue 首页与登录/注册页面 | 已完成 | 首页后端状态展示，登录/注册成功后保存 JWT。 |
| 最终启动冒烟测试 | 已完成 | MySQL 连接、后端启动、健康检查与 JWT 链路已验证。 |


### 运行

前提：JDK 17 与 Maven 3.9+。

```powershell
mvn spring-boot:run
```

按 [db/mysql/README.md](db/mysql/README.md) 中的编号顺序导入脚本：它们会创建 `your_microworld` 数据库及完整的 Sprint 0–5 表结构。默认连接 `localhost:3306/your_microworld`，本机密码位于已忽略且不会打进 JAR 的 `config/application-local.yml`；团队成员请通过 `DB_PASSWORD` 环境变量配置自己的密码。

如需临时不用 MySQL 的 H2 演示模式，可追加 `--spring.profiles.active=h2`；H2 控制台为 `http://localhost:8080/h2-console`。

另开一个终端启动前端（需 Node.js 20+）：

```powershell
cd frontend
npm install
npm run dev
```

浏览器访问 `http://localhost:5173`。Vite 已将 `/api` 代理到后端的 `8080` 端口。

### API

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/health` | 服务健康状态 |
| GET | `/api/home` | 首页占位内容 |
| POST | `/api/auth/register` | 注册（用户名、密码） |
| POST | `/api/auth/login` | 登录校验（用户名、密码） |
| GET | `/api/auth/me` | JWT 受保护接口，返回当前用户名 |

登录成功后，所有需要认证的接口须附带：

```http
Authorization: Bearer <accessToken>
```

注册请求示例：

```json
{
  "username": "writer_01",
  "password": "password123"
}
```

## 后续工程结构

当前采用便于 MVP 快速演进的模块化单体；功能稳定后，按领域拆成 `auth`、`user`、`novel`、`chapter`、`branch`、`bid`、`comment`、`analytics` 和 `tag` 包。前端计划使用 Vue + Pinia，并以 Cytoscape.js 或 D3.js 呈现章节图。

```text
Web UI (Vue + Graph View)
          │ REST API
Spring Boot: auth / novel / chapter / branch / bid / comment / analytics
          │
MySQL（正式环境） + Redis（缓存、排行）
```

热度指标将在 Sprint 5 以章节为单位聚合，例如：浏览 × 1 + 评论 × 3 + 点赞 × 2 + 竞标参与 × 5；它用于作者的作品内 Top 10 热度排行和结构图节点样式。
